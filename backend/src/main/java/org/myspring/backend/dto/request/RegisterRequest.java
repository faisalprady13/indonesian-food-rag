package org.myspring.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Email is required")
        @Pattern(
                regexp = "^[\\w.+-]+@([\\w-]+\\.)+[a-zA-Z]{2,}$",
                message = "Email must be a valid email address"
        )
        String email,

        @NotBlank(message = "Full name is required")
        String fullname,

        @NotBlank(message = "Password is required")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$",
                message = "Password must be at least 8 characters long and include an uppercase letter, a lowercase letter, a digit, and a special character"
        )
        String password
) {
}
