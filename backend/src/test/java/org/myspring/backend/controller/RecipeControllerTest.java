package org.myspring.backend.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myspring.backend.dto.response.RecipeResponse;
import org.myspring.backend.dto.response.RecipeDetailResponse;
import org.myspring.backend.dto.response.RecipeSuggestionResponse;
import org.myspring.backend.model.User;
import org.myspring.backend.model.UserPrincipal;
import org.myspring.backend.service.RecipeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeControllerTest {

    @Mock
    private RecipeService recipeService;

    @InjectMocks
    private RecipeController recipeController;

    private final UserPrincipal principal = new UserPrincipal(User.builder().id(1L).build());

    @Test
    void getRecipes_delegatesToServiceAndReturnsItsResult() {
        RecipeResponse rendang = new RecipeResponse(
                1L, "Rendang", "Simmer beef in coconut milk.", LocalDateTime.now(), LocalDateTime.now(), true
        );
        Page<RecipeResponse> page = new PageImpl<>(List.of(rendang));
        when(recipeService.getRecipes(0, 10, "id", "asc", "rendang", 1L)).thenReturn(page);

        ResponseEntity<Page<RecipeResponse>> result =
                recipeController.getRecipes(principal, 0, 10, "id", "asc", "rendang");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assert result.getBody() != null;
        assertThat(result.getBody().getContent()).containsExactly(rendang);
        verify(recipeService).getRecipes(0, 10, "id", "asc", "rendang", 1L);
    }

    @Test
    void getRecipes_passesNullSearchThrough_whenNotProvided() {
        Page<RecipeResponse> page = new PageImpl<>(List.of());
        when(recipeService.getRecipes(0, 10, "id", "asc", null, 1L)).thenReturn(page);

        ResponseEntity<Page<RecipeResponse>> result =
                recipeController.getRecipes(principal, 0, 10, "id", "asc", null);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assert result.getBody() != null;
        assertThat(result.getBody().getContent()).isEmpty();
        verify(recipeService).getRecipes(0, 10, "id", "asc", null, 1L);
    }

    @Test
    void getRecipe_delegatesToServiceAndReturnsItsResult() {
        RecipeDetailResponse rendang = new RecipeDetailResponse(
                1L, "Rendang", "Simmer beef in coconut milk.",
                List.of("beef", "coconut milk"), LocalDateTime.now(), LocalDateTime.now(), true
        );
        when(recipeService.getRecipe(1L, 1L)).thenReturn(rendang);

        ResponseEntity<RecipeDetailResponse> result = recipeController.getRecipe(principal, 1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(rendang);
        verify(recipeService).getRecipe(1L, 1L);
    }

    @Test
    void getFavoriteRecipes_delegatesToServiceAndReturnsItsResult() {
        RecipeResponse rendang = new RecipeResponse(
                1L, "Rendang", "Simmer beef in coconut milk.", LocalDateTime.now(), LocalDateTime.now(), true
        );
        Page<RecipeResponse> page = new PageImpl<>(List.of(rendang));
        when(recipeService.getFavoriteRecipes(1L, 0, 10, "rendang")).thenReturn(page);

        ResponseEntity<Page<RecipeResponse>> result =
                recipeController.getFavoriteRecipes(principal, 0, 10, "rendang");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assert result.getBody() != null;
        assertThat(result.getBody().getContent()).containsExactly(rendang);
        verify(recipeService).getFavoriteRecipes(1L, 0, 10, "rendang");
    }

    @Test
    void addFavorite_delegatesToServiceAndReturnsNoContent() {
        ResponseEntity<Void> result = recipeController.addFavorite(principal, 1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(recipeService).addFavorite(1L, 1L);
    }

    @Test
    void removeFavorite_delegatesToServiceAndReturnsNoContent() {
        ResponseEntity<Void> result = recipeController.removeFavorite(principal, 1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(recipeService).removeFavorite(1L, 1L);
    }

    @Test
    void autocomplete_delegatesToServiceWithQueryAndLimit() {
        RecipeSuggestionResponse suggestion = new RecipeSuggestionResponse(1L, "Rendang");
        when(recipeService.autocomplete("ren", 5)).thenReturn(List.of(suggestion));

        ResponseEntity<List<RecipeSuggestionResponse>> result = recipeController.autocomplete("ren", 5);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).containsExactly(suggestion);
        verify(recipeService).autocomplete("ren", 5);
    }

    @Test
    void autocomplete_returnsEmptyList_whenServiceFindsNoMatches() {
        when(recipeService.autocomplete("zzz", 10)).thenReturn(List.of());

        ResponseEntity<List<RecipeSuggestionResponse>> result = recipeController.autocomplete("zzz", 10);

        assertThat(result.getBody()).isEmpty();
    }
}
