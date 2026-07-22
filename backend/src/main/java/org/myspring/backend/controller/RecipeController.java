package org.myspring.backend.controller;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.request.ChatRequest;
import org.myspring.backend.dto.response.ChatResponse;
import org.myspring.backend.dto.response.RecipeResponse;
import org.myspring.backend.dto.response.RecipeDetailResponse;
import org.myspring.backend.dto.response.RecipeSuggestionResponse;
import org.myspring.backend.model.UserPrincipal;
import org.myspring.backend.service.RecipeService;
import org.myspring.backend.service.rag.RecipeChatService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipe")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;
    private final RecipeChatService recipeChatService;

    @GetMapping
    public ResponseEntity<Page<RecipeResponse>> getRecipes(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(
                recipeService.getRecipes(page, size, sortBy, direction, search, principal.user().getId())
        );
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<Void> addFavorite(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        recipeService.addFavorite(principal.user().getId(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/favorite")
    public ResponseEntity<Void> removeFavorite(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        recipeService.removeFavorite(principal.user().getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/favorites")
    public ResponseEntity<Page<RecipeResponse>> getFavoriteRecipes(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(
                recipeService.getFavoriteRecipes(principal.user().getId(), page, size, search)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeDetailResponse> getRecipe(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(recipeService.getRecipe(id, principal.user().getId()));
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

    @PostMapping("/ask")
    public ChatResponse search(
            @RequestBody ChatRequest request
    ) {
        String conversationId = request.conversationId() != null
                ? request.conversationId()
                : "guest-1";

        String answer = recipeChatService.askQuestion(conversationId, request.question());

        return ChatResponse.builder()
                .conversationId(conversationId)
                .answer(answer)
                .build();

    }
}
