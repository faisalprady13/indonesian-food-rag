package org.myspring.backend.dto.request;


public record RegisterRequest(String username, String email, String fullname, String password) {
}
