package org.myspring.backend.controller;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.request.UserSettingRequest;
import org.myspring.backend.model.UserPrincipal;
import org.myspring.backend.model.UserSetting;
import org.myspring.backend.service.UserSettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/user-setting")
@RequiredArgsConstructor
public class UserSettingController {
    private final UserSettingService userSettingService;

    @GetMapping
    public ResponseEntity<UserSetting> getUserSetting(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                userSettingService.findByUserId(principal.user().getId())
        );
    }

    @PutMapping("/")
    public ResponseEntity<UserSetting> updateUserSetting(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UserSettingRequest userSetting
    ) {
        return ResponseEntity.ok(
                userSettingService.save(principal.user().getId(), userSetting)
        );
    }
}
