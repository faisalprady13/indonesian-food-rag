package org.myspring.backend.tool;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.RecipeDto;
import org.myspring.backend.enums.RecipeListType;
import org.myspring.backend.exception.UnauthorizedException;
import org.myspring.backend.factory.EmbeddingModelFactory;
import org.myspring.backend.helper.VectorConverter;
import org.myspring.backend.model.Recipe;
import org.myspring.backend.repository.RecipeEmbeddingRepository;
import org.myspring.backend.repository.RecipeRepository;
import org.myspring.backend.service.ApiKeyEncryptionService;
import org.myspring.backend.service.RecipeSelectionCacheService;
import org.myspring.backend.service.UserService;
import org.myspring.backend.service.UserSettingService;
import org.myspring.backend.specification.RecipeSpecifications;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RecipeTools {

    private final RecipeEmbeddingRepository embeddingRepository;
    private final RecipeRepository recipeRepository;
    private final EmbeddingModelFactory embeddingModelFactory;
    private final UserService userService;
    private final UserSettingService userSettingService;
    private final ApiKeyEncryptionService apiKeyEncryptionService;
    private final RecipeSelectionCacheService recipeSelectionCacheService;

    @Value("${spring.ai.openai.embedding.model}")
    private String embeddingModelName;


    @Tool(description = """
            Searches the recipe database by general keywords, categories, ingredients, or dietary preferences (e.g., 'healthy', 'chicken', 'quick') and for recipes matching the user's request.
            
            Use this tool when the user:
            - wants recipe recommendations
            - asks what they can cook
            - searches by ingredients
            - searches by dish type
            - requests meal ideas
            
            The limit controls how many recipes are returned:
            - Use a small limit (3-5) for specific requests.
            - Use a larger limit (10-20) when the user asks for many ideas.
            
            Returns recipe titles and short summaries, ordered from best match to least relevant match.
            Summaries describe the dish based on ingredients and cooking steps.

            ALWAYS use this tool before suggesting a list of recipes to the user.
            Never invent recipes. Only suggest recipes returned by this tool.
            Display and number the results in the exact order returned - do not re-rank or reorder them.
            """)
    public List<RecipeDto> searchRecipes(String query, int limit, ToolContext toolContext)
            throws UnauthorizedException {

        Long userId = userService.getCurrentUserId();

        String apiKey = apiKeyEncryptionService.decrypt(
                userSettingService.findByUserId(userId).getApiKey()
        );

        EmbeddingModel embeddingModel = embeddingModelFactory.create(apiKey, embeddingModelName);

        float[] vector = embeddingModel.embed(query);

        List<Long> recipeIds = embeddingRepository.findSimilarRecipeIds(
                VectorConverter.toPgVector(vector),
                limit
        );

        if (recipeIds.isEmpty()) {
            return List.of();
        }

        Map<Long, RecipeDto> recipesById = recipeRepository.findAllById(recipeIds)
                .stream()
                .map(RecipeDto::fromRecipe)
                .collect(Collectors.toMap(RecipeDto::id, Function.identity()));

        // findAllById does not preserve the input ID order, so re-apply the
        // similarity ranking from recipeIds before displaying or caching the list.
        List<RecipeDto> recipes = recipeIds.stream()
                .map(recipesById::get)
                .filter(Objects::nonNull)
                .toList();

        recipeSelectionCacheService.store(
                conversationId(toolContext),
                RecipeListType.SEARCH_RESULTS,
                recipes.stream().map(RecipeDto::id).toList()
        );

        return recipes;
    }

    @Tool(description = """
            Finds recipes by title or recipe name.
    
            Use this tool when:
            - the user mentions a recipe name
            - the user wants to find a recipe by name
            - the recipe ID is needed but not available
            - the user wants to save or remove a recipe by name

            Returns matching recipes with their database IDs and recipe details.
    
            IMPORTANT RULES:
            - Never assume a recipe ID.
            - Never use a menu number, list position, or user selection number as the recipe ID.
            - If multiple recipes are returned, present them to the user as a numbered list.
            - If the user later refers to a result by its position (for example, "the second one"
              or "save recipe 2"), use the matching position-based tool (getRecipeByListPosition
              to view it, addFavoriteByListPosition to save it) instead of guessing the ID yourself.
            - Do not automatically choose a recipe when multiple matches exist.
            """)
    public List<RecipeDto> getRecipesByTitle(String title, ToolContext toolContext) throws UnauthorizedException {

        Specification<Recipe> spec =
                RecipeSpecifications.titleContainsAllWords(title);

        List<RecipeDto> recipes = recipeRepository.findAll(spec)
                .stream()
                .map(RecipeDto::fromRecipe)
                .toList();

        if (recipes.isEmpty()) {
            throw new IllegalArgumentException(
                    "No recipe found with title: " + title
            );
        }

        recipeSelectionCacheService.store(
                conversationId(toolContext),
                RecipeListType.SEARCH_RESULTS,
                recipes.stream().map(RecipeDto::id).toList()
        );

        return recipes;
    }

    @Tool(description = """
            Resolves a recipe by its position (1, 2, 3, ...) in the most recently displayed
            recipe list (from searchRecipes or getRecipesByTitle) and returns its full details.

            ALWAYS use this tool when the user refers to a recipe by its list position
            (e.g. "number 2", "the first one", "the third recipe") instead of guessing,
            recalling, or re-deriving the recipe's database ID yourself.

            If no recipe list has been shown yet, or the position is out of range,
            this tool will fail - in that case, ask the user to clarify or search again.
            """)
    public RecipeDto getRecipeByListPosition(int position, ToolContext toolContext) throws UnauthorizedException {

        Long recipeId = recipeSelectionCacheService.resolvePosition(
                conversationId(toolContext),
                RecipeListType.SEARCH_RESULTS,
                position
        );

        return recipeRepository.findById(recipeId)
                .map(RecipeDto::fromRecipe)
                .orElseThrow(() -> new IllegalArgumentException(
                        "The recipe previously shown at position " + position + " no longer exists."
                ));
    }

    private String conversationId(ToolContext toolContext) {
        return (String) toolContext.getContext().get("conversationId");
    }
}
