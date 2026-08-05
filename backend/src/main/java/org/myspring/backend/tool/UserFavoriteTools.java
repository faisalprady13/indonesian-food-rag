package org.myspring.backend.tool;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.response.FavoriteResponse;
import org.myspring.backend.dto.response.RecipeResponse;
import org.myspring.backend.enums.RecipeListType;
import org.myspring.backend.exception.UnauthorizedException;
import org.myspring.backend.service.RecipeSelectionCacheService;
import org.myspring.backend.service.RecipeService;
import org.myspring.backend.service.UserService;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
public class UserFavoriteTools {

    private final RecipeService recipeService;
    private final UserService userService;
    private final RecipeSelectionCacheService recipeSelectionCacheService;


    @Tool(description = """
        Adds a recipe to the user's favorite recipes.

        Use this tool only when the user explicitly wants to:
        - save a recipe
        - favorite a recipe
        - bookmark a recipe
        - remember a recipe

        Parameters:
        - recipeId: the actual database ID of the recipe.

        IMPORTANT:
        - recipeId MUST be the database ID returned by a previous tool.
        - NEVER use a menu number or list position (such as 1, 2, or 3) as recipeId.
        - If the user refers to the recipe by its position in a previously
          displayed list (for example, "save recipe 2"), use
          addFavoriteByListPosition instead - do not guess the ID here.
        - If the actual database ID cannot be determined with certainty,
          do NOT call this tool. Instead, ask the user which recipe they mean.
        - Never guess or invent a recipe ID.
        """)
    public FavoriteResponse addToFavorite(Long recipeId) throws UnauthorizedException {

        return recipeService.addFavorite(
                userService.getCurrentUserId(),
                recipeId
        );
    }

    @Tool(description = """
        Adds a recipe to the user's favorite recipes by its position (1, 2, 3, ...)
        in the most recently displayed recipe list (from searchRecipes or getRecipesByTitle).

        ALWAYS use this tool when the user selects a recipe to favorite by its list
        position (e.g. "save number 2", "favorite the first one") instead of guessing,
        recalling, or re-deriving the recipe's database ID yourself.

        If no recipe list has been shown yet, or the position is out of range,
        this tool will fail - in that case, ask the user to clarify or search again.
        """)
    public FavoriteResponse addFavoriteByListPosition(int position, ToolContext toolContext) throws UnauthorizedException {

        Long recipeId = recipeSelectionCacheService.resolvePosition(
                conversationId(toolContext),
                RecipeListType.SEARCH_RESULTS,
                position
        );

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

            If the user refers to the favorite by its position in the previously
            displayed favorites list (for example, "remove favorite 2"), use
            removeFavoriteByListPosition instead - do not guess the ID here.

            Never guess or invent a recipe ID.
            """)
    public void removeFromFavorite(Long recipeId) throws UnauthorizedException {

        recipeService.removeFavorite(
                userService.getCurrentUserId(),
                recipeId
        );
    }

    @Tool(description = """
            Removes a recipe from the user's favorite recipes by its position (1, 2, 3, ...)
            in the most recently displayed favorites list (from listFavorites).

            ALWAYS use this tool when the user refers to a favorite by its list position
            (e.g. "remove number 2", "delete the first one") instead of guessing,
            recalling, or re-deriving the recipe's database ID yourself.

            If listFavorites has not been called yet, or the position is out of range,
            this tool will fail - in that case, call listFavorites again or ask the user
            to clarify.
            """)
    public void removeFavoriteByListPosition(int position, ToolContext toolContext) throws UnauthorizedException {

        Long recipeId = recipeSelectionCacheService.resolvePosition(
                conversationId(toolContext),
                RecipeListType.FAVORITES,
                position
        );

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
    public List<RecipeResponse> listFavorites(String search, ToolContext toolContext) throws UnauthorizedException {

        Page<RecipeResponse> favorites =
                recipeService.getFavoriteRecipes(
                        userService.getCurrentUserId(),
                        0,
                        50,
                        search
                );

        List<RecipeResponse> content = favorites.getContent();

        recipeSelectionCacheService.store(
                conversationId(toolContext),
                RecipeListType.FAVORITES,
                content.stream().map(RecipeResponse::id).toList()
        );

        return content;
    }

    private String conversationId(ToolContext toolContext) {
        return (String) toolContext.getContext().get("conversationId");
    }
}
