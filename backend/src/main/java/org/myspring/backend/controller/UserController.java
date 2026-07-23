package org.myspring.backend.controller;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.UserDto;
import org.myspring.backend.dto.response.UserResponse;
import org.myspring.backend.exception.UserNotFound;
import org.myspring.backend.model.User;
import org.myspring.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @ModelAttribute UserDto userDto,
            @RequestPart(name = "file", required = false) MultipartFile file
    ) throws IOException, UserNotFound {
        User user;
        if (file == null || file.isEmpty()) {
            user = userService.updateUser(id, userDto);
        } else {
            user = userService.updateProfilePic(id, userDto, file);
        }
        return ResponseEntity.ok(UserResponse.fromUser(user));
    }

    @DeleteMapping("/{id}/{username}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @PathVariable String username
    ) throws UserNotFound {
        userService.deleteUser(id, username);
        return ResponseEntity.ok().build();
    }

}
