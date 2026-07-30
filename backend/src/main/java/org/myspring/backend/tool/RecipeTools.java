package org.myspring.backend.tool;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.RecipeDto;
import org.myspring.backend.dto.RecipeSelectionDto;
import org.myspring.backend.exception.UnauthorizedException;
import org.myspring.backend.factory.EmbeddingModelFactory;
import org.myspring.backend.helper.VectorConverter;
import org.myspring.backend.model.Recipe;
import org.myspring.backend.repository.RecipeEmbeddingRepository;
import org.myspring.backend.repository.RecipeRepository;
import org.myspring.backend.service.ApiKeyEncryptionService;
import org.myspring.backend.service.UserService;
import org.myspring.backend.service.UserSettingService;
import org.myspring.backend.specification.RecipeSpecifications;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RecipeTools {

    private final RecipeEmbeddingRepository embeddingRepository;
    private final RecipeRepository recipeRepository;
    private final EmbeddingModelFactory embeddingModelFactory;
    private final UserService userService;
    private final UserSettingService userSettingService;
    private final ApiKeyEncryptionService apiKeyEncryptionService;

    @Value("${spring.ai.openai.embedding.model}")
    private String embeddingModelName;


    @Tool(description = """
            Searches the recipe database for recipes matching the user's request.
            
            Use this tool when the user:
            - wants recipe recommendations
            - asks what they can cook
            - searches by ingredients
            - searches by dish type
            - requests meal ideas
            
            The search is based on recipe similarity.
            
            Returns recipe titles and short summaries.
            Summaries describe the dish based on ingredients and cooking steps.
            
            The limit controls how many recipes are returned:
            - Use a small limit (3-5) for specific requests.
            - Use a larger limit (10-20) when the user asks for many ideas.
            
            Recipe IDs are internal and must never be shown to the user.
            """)
    public List<RecipeDto> searchRecipes(String query, int limit)
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

        return recipeRepository.findAllById(recipeIds)
                .stream()
                .map(RecipeDto::fromRecipe)
                .toList();
    }

    @Tool(description = """
            Finds recipes by title or recipe name.
            
            Use this when:
            - the user mentions a recipe name
            - the recipe ID is needed but not available
            - the user wants to save or remove a recipe by name
            
            Returns matching recipes with only their IDs and titles.
            
            If multiple recipes are returned:
            - Ask the user which recipe they mean.
            - Do not choose one automatically.
            
            Never invent recipe IDs.
            """)
    public List<RecipeSelectionDto> getRecipesByTitle(String title) {

        Specification<Recipe> spec =
                RecipeSpecifications.titleContainsAllWords(title);

        List<RecipeSelectionDto> recipes = recipeRepository.findAll(spec)
                .stream()
                .map(recipe -> new RecipeSelectionDto(
                        recipe.getId(),
                        recipe.getTitle()
                ))
                .toList();

        if (recipes.isEmpty()) {
            throw new IllegalArgumentException(
                    "No recipe found with title: " + title
            );
        }

        return recipes;
    }
}
