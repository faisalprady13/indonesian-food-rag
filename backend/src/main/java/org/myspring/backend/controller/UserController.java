package org.myspring.backend.controller;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.UserResponse;
import org.myspring.backend.model.User;
import org.myspring.backend.model.UserPrincipal;
import org.myspring.backend.service.CloudinaryService;
import org.myspring.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {


    // TODO: put cloudinaryService inside userService.updateUser()
    private final UserService service;
    private final CloudinaryService cloudinaryService;

    @GetMapping
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(UserResponse.fromUser(principal.user()));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody User user) {
        return ResponseEntity.ok(
                UserResponse.fromUser(service.register(user))
        );
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        return ResponseEntity.ok(
                service.verify(user)
        );
    }

    @PostMapping("/upload-photo")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestPart(name = "file", required = false) MultipartFile file) throws IOException {

        String url = cloudinaryService.upload(file);
        // TODO: store to db
        // TODO: put cloudinaryService inside userService.updateUser()

        return ResponseEntity.ok(
                Map.of("url", url)
        );

    }
}
