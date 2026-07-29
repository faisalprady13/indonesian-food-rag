package org.myspring.backend.tool;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.RecipeDto;
import org.myspring.backend.exception.UnauthorizedException;
import org.myspring.backend.factory.EmbeddingModelFactory;
import org.myspring.backend.helper.VectorConverter;
import org.myspring.backend.repository.RecipeEmbeddingRepository;
import org.myspring.backend.repository.RecipeRepository;
import org.myspring.backend.service.ApiKeyEncryptionService;
import org.myspring.backend.service.UserService;
import org.myspring.backend.service.UserSettingService;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
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
            Searches the PostgreSQL vector database for matching food recipes.
            Only call this tool when the guest provides specific dish names,
            ingredients, or clear meal preferences.
            """)
    public List<RecipeDto> searchRecipes(String query) throws UnauthorizedException {

        Long userId = userService.getCurrentUserId();

        String apiKey = apiKeyEncryptionService.decrypt(
                userSettingService.findByUserId(userId).getApiKey()
        );

        EmbeddingModel embeddingModel = embeddingModelFactory.create(apiKey, embeddingModelName);

        // embed question
        float[] vector =
                embeddingModel.embed(query);

        // search vector
        List<Long> recipeIds =
                embeddingRepository.findSimilarRecipeIds(
                        VectorConverter.toPgVector(vector),
                        5
                );

        if (recipeIds.isEmpty()) {
            return new ArrayList<>();
        }

        // get by recipe id
        return recipeRepository.findAllById(recipeIds).stream()
                .map(RecipeDto::fromRecipe)
                .toList();

    }
}
