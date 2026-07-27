package org.myspring.backend.service;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.request.UserSettingRequest;
import org.myspring.backend.model.User;
import org.myspring.backend.model.UserSetting;
import org.myspring.backend.repository.UserRepository;
import org.myspring.backend.repository.UserSettingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserSettingService {
    private final UserSettingRepository userSettingRepository;
    private final ApiKeyEncryptionService apiKeyEncryptionService;
    private final UserRepository userRepository;

    @Transactional
    public UserSetting save(Long userId, UserSettingRequest userSettingRequest) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        UserSetting userSetting = userSettingRepository
                .findByUserId(userId)
                .orElseGet(() -> UserSetting.builder()
                        .user(user)
                        .build());

        userSetting.setAppTheme(userSettingRequest.appTheme());

        if (userSettingRequest.apiKey() != null && !userSettingRequest.apiKey().isBlank()) {
            String encryptedKey = apiKeyEncryptionService.encrypt(
                    userSettingRequest.apiKey()
            );

            userSetting.setApiKey(encryptedKey);
        }

        return userSettingRepository.save(userSetting);
    }

    @Transactional
    public UserSetting findByUserId(Long userId) {
        return userSettingRepository.findByUserId(userId).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User Setting not found"
        ));
    }
}
