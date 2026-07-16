package org.myspring.backend.service;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.RecipeDetailResponse;
import org.myspring.backend.dto.RecipeResponse;
import org.myspring.backend.dto.RecipeSuggestionResponse;
import org.myspring.backend.model.Recipe;
import org.myspring.backend.repository.RecipeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private final RecipeRepository recipeRepository;

    @Transactional(readOnly = true)
    public Page<RecipeResponse> getRecipes(int page, int size, String sortBy, String direction, String search) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Recipe> recipes = (search == null || search.isBlank())
                ? recipeRepository.findAll(pageable)
                : recipeRepository.findByTitleContainingIgnoreCase(search, pageable);

        return recipes.map(RecipeResponse::fromRecipe);
    }

    @Transactional(readOnly = true)
    public RecipeDetailResponse getRecipe(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found"));
        return RecipeDetailResponse.fromRecipe(recipe);
    }

    public List<RecipeSuggestionResponse> autocomplete(String query, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        Pageable pageable = PageRequest.of(0, limit);
        return recipeRepository.findByTitleContainingIgnoreCaseOrderByTitleAsc(query, pageable)
                .stream()
                .map(RecipeSuggestionResponse::fromRecipe)
                .toList();
    }
}
