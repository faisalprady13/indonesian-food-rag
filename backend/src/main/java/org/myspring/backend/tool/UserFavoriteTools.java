package org.myspring.backend.tool;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.response.FavoriteResponse;
import org.myspring.backend.dto.response.RecipeResponse;
import org.myspring.backend.exception.UnauthorizedException;
import org.myspring.backend.service.RecipeService;
import org.myspring.backend.service.UserService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
public class UserFavoriteTools {
    private final RecipeService recipeService;
    private final UserService userService;


    @Tool(description = """
            Save a recipe to the user's favorite recipes.
            Use this when the user asks to save, favorite, bookmark,
            or remember a recipe.
            Requires the recipe ID.
            Returns the result of the favorite operation.
            """)
    public FavoriteResponse addToFavorite(Long recipeId) throws UnauthorizedException {
        return recipeService.addFavorite(userService.getCurrentUserId(), recipeId);

    }

    @Tool(description = """
            Remove a recipe from the user's favorite recipes.
            Use this when the user asks to remove, unfavorite,
            or delete a recipe from their favorites.
            Requires the recipe ID.
            """)
    public void removeFromFavorite(Long recipeId) throws UnauthorizedException {
        recipeService.removeFavorite(userService.getCurrentUserId(), recipeId);
    }

    @Tool(description = """
            List the user's favorite recipes.
            Use this when the user asks to see, view, browse,
            or check their favorite recipes.
            Optionally filter by a recipe title search term.
            Returns the recipe titles and IDs.
            """)
    public List<RecipeResponse> listFavorites(String search) throws UnauthorizedException {
        Page<RecipeResponse> favorites = recipeService.getFavoriteRecipes(
                userService.getCurrentUserId(), 0, 50, search);
        return favorites.getContent();
    }

}
