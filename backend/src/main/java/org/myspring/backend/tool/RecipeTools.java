package org.myspring.backend.tool;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.RecipeDto;
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
            Returns recipes with their IDs.
            
            After receiving results:
            - Use the returned recipe IDs for other tools.
            - Never invent recipe IDs.
            """)
    public List<RecipeDto> searchRecipes(String query)
            throws UnauthorizedException {

        Long userId = userService.getCurrentUserId();

        String apiKey = apiKeyEncryptionService.decrypt(
                userSettingService.findByUserId(userId).getApiKey()
        );

        EmbeddingModel embeddingModel =
                embeddingModelFactory.create(apiKey, embeddingModelName);

        float[] vector = embeddingModel.embed(query);

        List<Long> recipeIds =
                embeddingRepository.findSimilarRecipeIds(
                        VectorConverter.toPgVector(vector),
                        5
                );

        if (recipeIds.isEmpty()) {
            return new ArrayList<>();
        }

        return recipeRepository.findAllById(recipeIds)
                .stream()
                .map(RecipeDto::fromRecipe)
                .toList();
    }


    @Tool(description = """
            Finds a specific recipe by its title.
            
            Use this tool when:
            - the user mentions a recipe name
            - the user wants to save, favorite, or remove a recipe
            - the recipe ID is required but not available
            
            Example:
            User: "Save Chicken Curry to my favorites"
            Action:
            1. Call this tool with "Chicken Curry".
            2. Use the returned recipe ID with the favorite tool.
            
            Returns the matching recipe including its ID.
            
            Never guess recipe IDs.
            """)
    public RecipeDto getRecipeByTitle(String title) {

        Specification<Recipe> spec =
                RecipeSpecifications.titleContainsAllWords(title);

        return recipeRepository.findAll(spec)
                .stream()
                .findFirst()
                .map(RecipeDto::fromRecipe)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No recipe found with title: " + title
                        )
                );
    }
}
