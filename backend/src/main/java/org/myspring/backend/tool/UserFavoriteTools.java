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
            Adds a recipe to the user's favorite recipes.
            
            Use this when the user explicitly wants to:
            - save a recipe
            - favorite a recipe
            - bookmark a recipe
            - remember a recipe
            
            The recipe ID must come from:
            - getRecipesByTitle
            
            Never:
            - guess a recipe ID
            - create a recipe ID yourself
            - add a recipe that was not identified by a tool result
            """)
    public FavoriteResponse addToFavorite(Long recipeId) throws UnauthorizedException {

        return recipeService.addFavorite(
                userService.getCurrentUserId(),
                recipeId
        );
    }


    @Tool(description = """
            Removes a recipe from the user's favorite recipes.
            
            Use this when the user explicitly wants to:
            - remove a favorite recipe
            - unfavorite a recipe
            - delete a recipe from favorites
            
            Before calling this tool:
            - A specific recipe must be identified.
            - The recipe ID must come from:
                - listFavorites
            
            If the user provides only a recipe name:
            - First call listFavorites to find the matching favorite recipe.
            - Then use the returned recipe ID.
            
            Never guess or invent a recipe ID.
            """)
    public void removeFromFavorite(Long recipeId) throws UnauthorizedException {

        recipeService.removeFavorite(
                userService.getCurrentUserId(),
                recipeId
        );
    }


    @Tool(description = """
            Retrieves the user's favorite recipes.
            
            Use this when the user asks to:
            - see favorites
            - view saved recipes
            - browse favorite recipes
            - check saved recipes
            
            Returns recipes with their IDs and titles.
            
            When displaying results to the user:
            - Show recipe titles only.
            - Do not show IDs unless explicitly requested.
            """)
    public List<RecipeResponse> listFavorites(String search) throws UnauthorizedException {

        Page<RecipeResponse> favorites =
                recipeService.getFavoriteRecipes(
                        userService.getCurrentUserId(),
                        0,
                        50,
                        search
                );

        return favorites.getContent();
    }
}
