package org.myspring.backend.dto;

public record AuthResponse(
        String token,
        String tokenType,
        String username,
        String email,
        long expiresIn
) {
}
