package org.myspring.backend.tool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myspring.backend.dto.response.FavoriteResponse;
import org.myspring.backend.dto.response.RecipeResponse;
import org.myspring.backend.enums.RecipeListType;
import org.myspring.backend.exception.UnauthorizedException;
import org.myspring.backend.service.RecipeSelectionCacheService;
import org.myspring.backend.service.RecipeService;
import org.myspring.backend.service.UserService;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserFavoriteToolsTest {

    private static final String CONVERSATION_ID = "10";
    private static final ToolContext TOOL_CONTEXT = new ToolContext(Map.of("conversationId", CONVERSATION_ID));

    @Mock
    private RecipeService recipeService;

    @Mock
    private UserService userService;

    @Mock
    private RecipeSelectionCacheService recipeSelectionCacheService;

    @InjectMocks
    private UserFavoriteTools userFavoriteTools;

    @Test
    void addToFavorite_delegatesDirectlyToRecipeService_withGivenRecipeId() throws UnauthorizedException {
        when(userService.getCurrentUserId()).thenReturn(1L);
        FavoriteResponse expected = FavoriteResponse.builder().recipeId(99L).recipeName("Nasi Goreng").saved(true).build();
        when(recipeService.addFavorite(1L, 99L)).thenReturn(expected);

        FavoriteResponse result = userFavoriteTools.addToFavorite(99L);

        assertThat(result).isEqualTo(expected);
        verify(recipeSelectionCacheService, never()).resolvePosition(any(), any(), anyInt());
    }

    @Test
    void addFavoriteByListPosition_resolvesPositionFromSearchResultsCache_thenAddsFavorite() throws UnauthorizedException {
        when(recipeSelectionCacheService.resolvePosition(CONVERSATION_ID, RecipeListType.SEARCH_RESULTS, 2))
                .thenReturn(77L);
        when(userService.getCurrentUserId()).thenReturn(1L);
        FavoriteResponse expected = FavoriteResponse.builder().recipeId(77L).recipeName("Rendang").saved(true).build();
        when(recipeService.addFavorite(1L, 77L)).thenReturn(expected);

        FavoriteResponse result = userFavoriteTools.addFavoriteByListPosition(2, TOOL_CONTEXT);

        assertThat(result).isEqualTo(expected);
        verify(recipeService).addFavorite(1L, 77L);
    }

    @Test
    void removeFromFavorite_delegatesDirectlyToRecipeService_withGivenRecipeId() throws UnauthorizedException {
        when(userService.getCurrentUserId()).thenReturn(1L);

        userFavoriteTools.removeFromFavorite(99L);

        verify(recipeService).removeFavorite(1L, 99L);
    }

    @Test
    void removeFavoriteByListPosition_resolvesPositionFromFavoritesCache_notSearchResults() throws UnauthorizedException {
        when(recipeSelectionCacheService.resolvePosition(CONVERSATION_ID, RecipeListType.FAVORITES, 1))
                .thenReturn(55L);
        when(userService.getCurrentUserId()).thenReturn(1L);

        userFavoriteTools.removeFavoriteByListPosition(1, TOOL_CONTEXT);

        verify(recipeSelectionCacheService).resolvePosition(CONVERSATION_ID, RecipeListType.FAVORITES, 1);
        verify(recipeSelectionCacheService, never())
                .resolvePosition(CONVERSATION_ID, RecipeListType.SEARCH_RESULTS, 1);
        verify(recipeService).removeFavorite(1L, 55L);
    }

    @Test
    void listFavorites_storesResultIntoFavoritesCache_keyedByConversationId() throws UnauthorizedException {
        when(userService.getCurrentUserId()).thenReturn(1L);
        RecipeResponse first = new RecipeResponse(1L, "Recipe A", "steps", null, null, true);
        RecipeResponse second = new RecipeResponse(2L, "Recipe B", "steps", null, null, true);
        when(recipeService.getFavoriteRecipes(1L, 0, 50, "nasi"))
                .thenReturn(new PageImpl<>(List.of(first, second)));

        List<RecipeResponse> result = userFavoriteTools.listFavorites("nasi", TOOL_CONTEXT);

        assertThat(result).containsExactly(first, second);
        verify(recipeSelectionCacheService)
                .store(CONVERSATION_ID, RecipeListType.FAVORITES, List.of(1L, 2L));
    }
}
