package org.myspring.backend.service.rag;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.response.RecipeAskResponse;
import org.myspring.backend.helper.VectorConverter;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import org.myspring.backend.repository.RecipeEmbeddingRepository;
import org.myspring.backend.repository.RecipeRepository;


@Service
@RequiredArgsConstructor
public class RagService {

    private final EmbeddingService embeddingService;

    private final RecipeEmbeddingRepository embeddingRepository;

    private final RecipeRepository recipeRepository;


    public List<RecipeAskResponse> retrieve(String question) {


        // 1. Convert question into vector

        float[] vector =
                embeddingService.embed(question);


        // 2. Search similar recipes

        List<Long> recipeIds =
                embeddingRepository.findSimilarRecipeIds(
                        VectorConverter.toPgVector(vector),
                        5
                );


        // 3. Load recipes

        return recipeRepository.findAllById(recipeIds).stream()
                .map(RecipeAskResponse::fromRecipe)
                .toList();

    }

}