package org.myspring.backend.dto;

import org.myspring.backend.model.Ingredient;
import org.myspring.backend.model.Recipe;

import java.util.List;

public record RecipeDto(
        Long id,
        String title,
        String steps,
        List<String> ingredients
) {

    public static RecipeDto fromRecipe(Recipe recipe) {
        return new RecipeDto(
                recipe.getId(),
                recipe.getTitle(),
                recipe.getSteps(),
                recipe.getIngredients().stream().map(Ingredient::getName).toList()
        );
    }
}