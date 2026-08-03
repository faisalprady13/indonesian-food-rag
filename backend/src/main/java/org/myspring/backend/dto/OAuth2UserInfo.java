package org.myspring.backend.dto;

public record OAuth2UserInfo(String provider, String email, String username, String imageUrl, String fullName) {
}
