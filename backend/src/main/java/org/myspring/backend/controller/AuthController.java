package org.myspring.backend.controller;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.request.LoginRequest;
import org.myspring.backend.dto.request.RegisterRequest;
import org.myspring.backend.dto.response.UserResponse;
import org.myspring.backend.model.UserPrincipal;
import org.myspring.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;

    @GetMapping
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(UserResponse.fromUser(principal.user()));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest user) {
        return ResponseEntity.ok(
                UserResponse.fromUser(service.register(user))
        );
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest user) {
        return ResponseEntity.ok(
                service.verify(user)
        );
    }
}
