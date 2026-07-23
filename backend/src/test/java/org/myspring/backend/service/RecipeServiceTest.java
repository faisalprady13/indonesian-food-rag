package org.myspring.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myspring.backend.dto.response.RecipeDetailResponse;
import org.myspring.backend.dto.response.RecipeResponse;
import org.myspring.backend.dto.response.RecipeSuggestionResponse;
import org.myspring.backend.model.Ingredient;
import org.myspring.backend.model.Recipe;
import org.myspring.backend.model.User;
import org.myspring.backend.repository.RecipeRepository;
import org.myspring.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RecipeService recipeService;

    private Recipe newRecipe(Long id, String title, String... ingredientNames) {
        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setTitle(title);
        recipe.setSteps("Simmer and serve.");
        recipe.setCreatedAt(LocalDateTime.now());
        recipe.setUpdatedAt(LocalDateTime.now());
        Set<Ingredient> ingredients = Set.of();
        if (ingredientNames.length > 0) {
            ingredients = java.util.Arrays.stream(ingredientNames)
                    .map(name -> {
                        Ingredient ingredient = new Ingredient();
                        ingredient.setName(name);
                        ingredient.setRecipe(recipe);
                        return ingredient;
                    })
                    .collect(java.util.stream.Collectors.toSet());
        }
        recipe.setIngredients(ingredients);
        return recipe;
    }

    @Test
    void getRecipes_returnsAllRecipesPaginated_whenSearchIsBlank() {
        Recipe rendang = newRecipe(1L, "Rendang");
        Recipe soto = newRecipe(2L, "Soto Ayam");
        Pageable pageable = PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("id").ascending());
        when(recipeRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(rendang, soto)));
        when(userRepository.findFavoriteRecipeIds(1L)).thenReturn(Set.of());

        Page<RecipeResponse> result = recipeService.getRecipes(0, 10, "id", "asc", null, 1L);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(RecipeResponse::title).containsExactly("Rendang", "Soto Ayam");
        verify(recipeRepository, never()).findByTitleContainingIgnoreCase(any(), any());
    }

    @Test
    void getRecipes_filtersBySearchTerm() {
        Recipe rendang = newRecipe(1L, "Rendang");
        Pageable pageable = PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("id").ascending());
        when(recipeRepository.findByTitleContainingIgnoreCase(eq("rendang"), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(rendang)));
        when(userRepository.findFavoriteRecipeIds(1L)).thenReturn(Set.of());

        Page<RecipeResponse> result = recipeService.getRecipes(0, 10, "id", "asc", "rendang", 1L);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().title()).isEqualTo("Rendang");
    }

    @Test
    void getRecipes_marksFavoritedRecipe() {
        Recipe rendang = newRecipe(1L, "Rendang");
        Recipe soto = newRecipe(2L, "Soto Ayam");
        Pageable pageable = PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("id").ascending());
        when(recipeRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(rendang, soto)));
        when(userRepository.findFavoriteRecipeIds(1L)).thenReturn(Set.of(1L));

        Page<RecipeResponse> result = recipeService.getRecipes(0, 10, "id", "asc", null, 1L);

        assertThat(result.getContent().get(0).favorited()).isTrue();
        assertThat(result.getContent().get(1).favorited()).isFalse();
    }

    @Test
    void getRecipe_returnsRecipeWithIngredients() {
        Recipe rendang = newRecipe(1L, "Rendang", "beef", "coconut milk", "chili");
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(rendang));
        when(userRepository.findFavoriteRecipeIds(1L)).thenReturn(Set.of());

        RecipeDetailResponse result = recipeService.getRecipe(1L, 1L);

        assertThat(result.title()).isEqualTo("Rendang");
        assertThat(result.ingredients()).hasSize(3);
        assertThat(result.favorited()).isFalse();
    }

    @Test
    void getRecipe_throwsNotFound_whenRecipeDoesNotExist() {
        when(recipeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> recipeService.getRecipe(999L, 1L));
    }

    @Test
    void addFavorite_addsRecipeToUserFavorites() {
        User user = User.builder().id(1L).build();
        Recipe recipe = newRecipe(2L, "Rendang");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(recipeRepository.findById(2L)).thenReturn(Optional.of(recipe));

        recipeService.addFavorite(1L, 2L);

        assertThat(user.getFavoriteRecipes()).contains(recipe);
        verify(userRepository).save(user);
    }

    @Test
    void addFavorite_throwsNotFound_whenUserDoesNotExist() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> recipeService.addFavorite(999L, 1L));
        verify(userRepository, never()).save(any());
    }

    @Test
    void addFavorite_throwsNotFound_whenRecipeDoesNotExist() {
        User user = User.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(recipeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> recipeService.addFavorite(1L, 999L));
        verify(userRepository, never()).save(any());
    }

    @Test
    void removeFavorite_removesRecipeFromUserFavorites() {
        Recipe recipe = newRecipe(2L, "Rendang");
        User user = User.builder().id(1L).build();
        user.getFavoriteRecipes().add(recipe);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        recipeService.removeFavorite(1L, 2L);

        assertThat(user.getFavoriteRecipes()).doesNotContain(recipe);
        verify(userRepository).save(user);
    }

    @Test
    void removeFavorite_throwsNotFound_whenUserDoesNotExist() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> recipeService.removeFavorite(999L, 1L));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getFavoriteRecipes_returnsOnlyFavoritedRecipes() {
        Recipe rendang = newRecipe(1L, "Rendang");
        Pageable pageable = PageRequest.of(0, 10);
        when(recipeRepository.findByFavoritedByUsers_Id(1L, pageable)).thenReturn(new PageImpl<>(List.of(rendang)));

        Page<RecipeResponse> result = recipeService.getFavoriteRecipes(1L, 0, 10, null);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().title()).isEqualTo("Rendang");
        assertThat(result.getContent().getFirst().favorited()).isTrue();
    }

    @Test
    void getFavoriteRecipes_filtersBySearchTerm() {
        Recipe soto = newRecipe(2L, "Soto Ayam");
        Pageable pageable = PageRequest.of(0, 10);
        when(recipeRepository.findByFavoritedByUsers_IdAndTitleContainingIgnoreCase(1L, "soto", pageable))
                .thenReturn(new PageImpl<>(List.of(soto)));

        Page<RecipeResponse> result = recipeService.getFavoriteRecipes(1L, 0, 10, "soto");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().title()).isEqualTo("Soto Ayam");
        verify(recipeRepository, never()).findByFavoritedByUsers_Id(anyLong(), any());
    }

    @Test
    void getFavoriteRecipes_returnsEmptyPage_whenNothingFavorited() {
        Pageable pageable = PageRequest.of(0, 10);
        when(recipeRepository.findByFavoritedByUsers_Id(1L, pageable)).thenReturn(Page.empty());

        Page<RecipeResponse> result = recipeService.getFavoriteRecipes(1L, 0, 10, null);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void autocomplete_returnsEmptyList_whenQueryIsBlank() {
        List<RecipeSuggestionResponse> result = recipeService.autocomplete("   ", 10);

        assertThat(result).isEmpty();
        verify(recipeRepository, never())
                .findAll(Mockito.<Specification<Recipe>>any(), any(Pageable.class));
    }

    @Test
    void autocomplete_returnsMappedSuggestions_whenQueryProvided() {
        Recipe soto = newRecipe(2L, "Soto Ayam");
        when(recipeRepository.findAll(Mockito.<Specification<Recipe>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(soto)));

        List<RecipeSuggestionResponse> result = recipeService.autocomplete("soto", 10);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().title()).isEqualTo("Soto Ayam");
    }

    @Test
    void autocomplete_respectsLimitParameter() {
        Recipe soto = newRecipe(2L, "Soto Ayam");
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(recipeRepository.findAll(Mockito.<Specification<Recipe>>any(), pageableCaptor.capture()))
                .thenReturn(new PageImpl<>(List.of(soto)));

        recipeService.autocomplete("soto", 1);

        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(1);
    }
}
