package org.myspring.backend.controller;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.request.UserSettingRequest;
import org.myspring.backend.dto.response.UserApiKeyStatusResponse;
import org.myspring.backend.dto.response.UserSettingResponse;
import org.myspring.backend.model.UserPrincipal;
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
    public ResponseEntity<UserSettingResponse> getUserSetting(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                UserSettingResponse.fromUserSetting(userSettingService.findByUserId(principal.user().getId()))
        );
    }

    @GetMapping("/key-status")
    public ResponseEntity<UserApiKeyStatusResponse> getOpenAiApiKeyStatus(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                UserApiKeyStatusResponse.fromUserSetting(userSettingService.findByUserId(principal.user().getId()))
        );
    }

    @PutMapping("/")
    public ResponseEntity<UserSettingResponse> updateUserSetting(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UserSettingRequest userSetting
    ) {
        return ResponseEntity.ok(
                UserSettingResponse.fromUserSetting(
                        userSettingService.save(principal.user().getId(), userSetting)
                )
        );
    }
}
