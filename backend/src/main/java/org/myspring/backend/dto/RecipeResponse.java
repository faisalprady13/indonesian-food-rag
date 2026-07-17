package org.myspring.backend.dto;

import org.myspring.backend.model.Recipe;

import java.time.LocalDateTime;

public record RecipeResponse(
        Long id,
        String title,
        String steps,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean favorited
) {

    public static RecipeResponse fromRecipe(Recipe recipe, boolean favorited) {
        return new RecipeResponse(
                recipe.getId(),
                recipe.getTitle(),
                recipe.getSteps(),
                recipe.getCreatedAt(),
                recipe.getUpdatedAt(),
                favorited
        );
    }
}
