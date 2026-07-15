package org.myspring.backend.dto;

import org.myspring.backend.model.Recipe;

public record RecipeSuggestionResponse(Long id, String title) {

    public static RecipeSuggestionResponse fromRecipe(Recipe recipe) {
        return new RecipeSuggestionResponse(recipe.getId(), recipe.getTitle());
    }
}
