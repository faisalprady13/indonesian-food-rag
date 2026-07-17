package org.myspring.backend.controller;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.UserDto;
import org.myspring.backend.dto.response.UserResponse;
import org.myspring.backend.exception.UserNotFound;
import org.myspring.backend.model.User;
import org.myspring.backend.model.UserPrincipal;
import org.myspring.backend.service.AuthService;
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
    private final UserService userService;

    @PutMapping("/update")
    public ResponseEntity<User> updateUser(
            @ModelAttribute UserDto userDto,
            @RequestPart(name = "file", required = false) MultipartFile file
    ) throws IOException, UserNotFound {
        User user;
        System.out.println(userDto);
        if (file == null || file.isEmpty()) {
            user = userService.updateUser(userDto);
        } else {
            user = userService.updateProfilePic(userDto, file);
        }
        return ResponseEntity.ok(user);
    }
}
