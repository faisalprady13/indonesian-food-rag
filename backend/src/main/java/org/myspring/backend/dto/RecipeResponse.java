package org.myspring.backend.dto;

import org.myspring.backend.model.Ingredient;
import org.myspring.backend.model.Recipe;

import java.time.LocalDateTime;
import java.util.List;

public record RecipeResponse(Long id, String title, String steps, List<String> ingredients,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {

    public static RecipeResponse fromRecipe(Recipe recipe) {
        return new RecipeResponse(
                recipe.getId(),
                recipe.getTitle(),
                recipe.getSteps(),
                recipe.getIngredients().stream().map(Ingredient::getName).toList(),
                recipe.getCreatedAt(),
                recipe.getUpdatedAt()
        );
    }
}
