package org.myspring.backend.controller;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.RecipeDetailResponse;
import org.myspring.backend.dto.RecipeResponse;
import org.myspring.backend.dto.RecipeSuggestionResponse;
import org.myspring.backend.service.RecipeService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recipe")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping
    public ResponseEntity<Page<RecipeResponse>> getRecipes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(
                recipeService.getRecipes(page, size, sortBy, direction, search)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeDetailResponse> getRecipe(@PathVariable Long id) {
        return ResponseEntity.ok(recipeService.getRecipe(id));
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<List<RecipeSuggestionResponse>> autocomplete(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(
                recipeService.autocomplete(query, limit)
        );
    }
}
