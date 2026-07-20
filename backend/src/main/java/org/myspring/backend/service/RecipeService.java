package org.myspring.backend.service;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.response.RecipeDetailResponse;
import org.myspring.backend.dto.response.RecipeResponse;
import org.myspring.backend.dto.response.RecipeSuggestionResponse;
import org.myspring.backend.model.Recipe;
import org.myspring.backend.model.User;
import org.myspring.backend.repository.RecipeRepository;
import org.myspring.backend.repository.UserRepository;
import org.myspring.backend.specification.RecipeSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<RecipeResponse> getRecipes(int page, int size, String sortBy, String direction, String search, Long currentUserId) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Recipe> recipes = (search == null || search.isBlank())
                ? recipeRepository.findAll(pageable)
                : recipeRepository.findByTitleContainingIgnoreCase(search, pageable);

        Set<Long> favoriteIds = userRepository.findFavoriteRecipeIds(currentUserId);
        return recipes.map(recipe -> RecipeResponse.fromRecipe(recipe, favoriteIds.contains(recipe.getId())));
    }

    @Transactional(readOnly = true)
    public Page<RecipeResponse> getFavoriteRecipes(Long userId, int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Recipe> recipes = (search == null || search.isBlank())
                ? recipeRepository.findByFavoritedByUsers_Id(userId, pageable)
                : recipeRepository.findByFavoritedByUsers_IdAndTitleContainingIgnoreCase(userId, search, pageable);

        return recipes.map(recipe -> RecipeResponse.fromRecipe(recipe, true));
    }

    @Transactional
    public void addFavorite(Long userId, Long recipeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
        user.getFavoriteRecipes().add(recipe);
        userRepository.save(user);
    }

    @Transactional
    public void removeFavorite(Long userId, Long recipeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.getFavoriteRecipes().removeIf(recipe -> recipe.getId().equals(recipeId));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public RecipeDetailResponse getRecipe(Long id, Long currentUserId) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
        boolean favorited = userRepository.findFavoriteRecipeIds(currentUserId).contains(recipe.getId());
        return RecipeDetailResponse.fromRecipe(recipe, favorited);
    }

    @Transactional(readOnly = true)
    public List<RecipeSuggestionResponse> autocomplete(String query, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        Pageable pageable = PageRequest.of(0, limit, Sort.by("title").ascending());
        Specification<Recipe> spec = RecipeSpecifications.titleContainsAllWords(query);
        return recipeRepository.findAll(spec, pageable)
                .stream()
                .map(RecipeSuggestionResponse::fromRecipe)
                .toList();
    }
}
