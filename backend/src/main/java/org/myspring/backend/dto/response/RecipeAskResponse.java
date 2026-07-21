package org.myspring.backend.dto.response;

import org.myspring.backend.model.Ingredient;
import org.myspring.backend.model.Recipe;

import java.util.List;

public record RecipeAskResponse(
        Long id,
        String title,
        String steps,
        List<String> ingredients
) {

    public static RecipeAskResponse fromRecipe(Recipe recipe) {
        return new RecipeAskResponse(
                recipe.getId(),
                recipe.getTitle(),
                recipe.getSteps(),
                recipe.getIngredients().stream().map(Ingredient::getName).toList()
        );
    }
}