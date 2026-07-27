package org.myspring.backend.dto.response;

import lombok.Builder;

@Builder
public record FavoriteResponse(
        Long recipeId,
        String recipeName,
        boolean saved
) {
}