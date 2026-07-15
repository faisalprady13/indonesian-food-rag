package org.myspring.backend.controller;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.RecipeResponse;
import org.myspring.backend.dto.RecipeSuggestionResponse;
import org.myspring.backend.service.RecipeService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
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
    public Page<RecipeResponse> getRecipes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String search
    ) {
        return recipeService.getRecipes(page, size, sortBy, direction, search);
    }

    @GetMapping("/autocomplete")
    public List<RecipeSuggestionResponse> autocomplete(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return recipeService.autocomplete(query, limit);
    }
}
