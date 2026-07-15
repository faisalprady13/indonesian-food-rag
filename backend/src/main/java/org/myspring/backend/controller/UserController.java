package org.myspring.backend.controller;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.UserResponse;
import org.myspring.backend.model.User;
import org.myspring.backend.model.UserPrincipal;
import org.myspring.backend.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping
    public UserResponse getMe(@AuthenticationPrincipal UserPrincipal principal) {
        return UserResponse.fromUser(principal.user());
    }

    @PostMapping("/register")
    public UserResponse register(@RequestBody User user) {
        return UserResponse.fromUser(service.register(user));
    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        return service.verify(user);
    }
}
