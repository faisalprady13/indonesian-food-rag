package org.myspring.backend.service.rag;

import java.util.List;

import org.myspring.backend.helper.VectorConverter;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import org.myspring.backend.model.Recipe;
import org.myspring.backend.repository.RecipeEmbeddingRepository;
import org.myspring.backend.repository.RecipeRepository;


@Service
public class RagService {


    private final EmbeddingModel embeddingModel;

    private final RecipeEmbeddingRepository embeddingRepository;

    private final RecipeRepository recipeRepository;


    public RagService(
            EmbeddingModel embeddingModel,
            RecipeEmbeddingRepository embeddingRepository,
            RecipeRepository recipeRepository
    ) {

        this.embeddingModel = embeddingModel;
        this.embeddingRepository = embeddingRepository;
        this.recipeRepository = recipeRepository;

    }


    public List<Recipe> retrieve(String question) {


        // 1. Convert question into vector

        float[] vector =
                embeddingModel.embed(question);


        // 2. Search similar recipes

        List<Long> recipeIds =
                embeddingRepository.findSimilarRecipeIds(
                        VectorConverter.toPgVector(vector),
                        5
                );


        // 3. Load recipes

        return recipeRepository.findAllById(recipeIds);

    }

}