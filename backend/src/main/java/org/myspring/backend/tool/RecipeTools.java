package org.myspring.backend.tool;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.response.RecipeAskResponse;
import org.myspring.backend.helper.VectorConverter;
import org.myspring.backend.repository.RecipeEmbeddingRepository;
import org.myspring.backend.repository.RecipeRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.embedding.EmbeddingModel;

@Component
@RequiredArgsConstructor
public class RecipeTools {
    private final RecipeEmbeddingRepository embeddingRepository;
    private final EmbeddingModel embeddingModel;
    private final RecipeRepository recipeRepository;


    @Tool(description = """
            Searches the PostgreSQL vector database for matching food recipes.
            Only call this tool when the guest provides specific dish names,
            ingredients, or clear meal preferences.
            """)
    public List<RecipeAskResponse> searchRecipes(String query) {

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
                .map(RecipeAskResponse::fromRecipe)
                .toList();

    }
}