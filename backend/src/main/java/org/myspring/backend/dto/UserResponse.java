package org.myspring.backend.dto;

import org.myspring.backend.model.User;

public record UserResponse(String username, String email, String imageUrl, String provider) {

    public static UserResponse fromUser(User user) {
        return new UserResponse(user.getUsername(), user.getEmail(), user.getImageUrl(), user.getProvider());
    }
}
