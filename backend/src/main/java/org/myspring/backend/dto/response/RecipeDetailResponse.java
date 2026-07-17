package org.myspring.backend.dto.response;

import org.myspring.backend.model.Ingredient;
import org.myspring.backend.model.Recipe;

import java.time.LocalDateTime;
import java.util.List;

public record RecipeDetailResponse(
        Long id,
        String title,
        String steps,
        List<String> ingredients,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean favorited
) {

    public static RecipeDetailResponse fromRecipe(Recipe recipe, boolean favorited) {
        return new RecipeDetailResponse(
                recipe.getId(),
                recipe.getTitle(),
                recipe.getSteps(),
                recipe.getIngredients().stream().map(Ingredient::getName).toList(),
                recipe.getCreatedAt(),
                recipe.getUpdatedAt(),
                favorited
        );
    }
}
