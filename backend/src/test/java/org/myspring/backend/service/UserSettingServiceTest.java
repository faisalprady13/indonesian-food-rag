package org.myspring.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myspring.backend.dto.request.UserSettingRequest;
import org.myspring.backend.model.User;
import org.myspring.backend.model.UserSetting;
import org.myspring.backend.repository.UserRepository;
import org.myspring.backend.repository.UserSettingRepository;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSettingServiceTest {

    @Mock
    private UserSettingRepository userSettingRepository;

    @Mock
    private ApiKeyEncryptionService apiKeyEncryptionService;

    @Mock
    private UserRepository userRepository;

    private UserSettingService userSettingService;

    @BeforeEach
    void setUp() {
        userSettingService = new UserSettingService(userSettingRepository, apiKeyEncryptionService, userRepository);
    }

    @Test
    void save_createsNewUserSetting_whenNoneExists() {
        User user = User.builder().id(1L).build();
        UserSettingRequest request = new UserSettingRequest("light", null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userSettingRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userSettingRepository.save(any(UserSetting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserSetting result = userSettingService.save(1L, request);

        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getAppTheme()).isEqualTo("light");
        assertThat(result.getApiKey()).isNull();
        verify(apiKeyEncryptionService, never()).encrypt(any());
    }

    @Test
    void save_updatesExistingUserSetting_whenFound() {
        User user = User.builder().id(1L).build();
        UserSetting existing = UserSetting.builder().id(5L).user(user).appTheme("dark").build();
        UserSettingRequest request = new UserSettingRequest("light", null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userSettingRepository.findByUserId(1L)).thenReturn(Optional.of(existing));
        when(userSettingRepository.save(any(UserSetting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserSetting result = userSettingService.save(1L, request);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getAppTheme()).isEqualTo("light");
    }

    @Test
    void save_encryptsApiKey_whenApiKeyProvided() {
        User user = User.builder().id(1L).build();
        UserSettingRequest request = new UserSettingRequest("dark", "my-openai-key");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userSettingRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(apiKeyEncryptionService.encrypt("my-openai-key")).thenReturn("encrypted-value");
        when(userSettingRepository.save(any(UserSetting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserSetting result = userSettingService.save(1L, request);

        assertThat(result.getApiKey()).isEqualTo("encrypted-value");
        verify(apiKeyEncryptionService).encrypt("my-openai-key");
    }

    @Test
    void save_doesNotEncryptApiKey_whenApiKeyBlank() {
        User user = User.builder().id(1L).build();
        UserSettingRequest request = new UserSettingRequest("dark", "   ");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userSettingRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userSettingRepository.save(any(UserSetting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserSetting result = userSettingService.save(1L, request);

        assertThat(result.getApiKey()).isNull();
        verify(apiKeyEncryptionService, never()).encrypt(any());
    }

    @Test
    void save_throwsNotFound_whenUserDoesNotExist() {
        UserSettingRequest request = new UserSettingRequest("dark", null);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> userSettingService.save(999L, request));
        verify(userSettingRepository, never()).save(any());
    }

    @Test
    void findByUserId_returnsUserSetting_whenFound() {
        User user = User.builder().id(1L).build();
        UserSetting setting = UserSetting.builder().id(5L).user(user).appTheme("dark").build();
        when(userSettingRepository.findByUserId(1L)).thenReturn(Optional.of(setting));

        UserSetting result = userSettingService.findByUserId(1L);

        assertThat(result).isEqualTo(setting);
    }

    @Test
    void findByUserId_throwsNotFound_whenNotFound() {
        when(userSettingRepository.findByUserId(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> userSettingService.findByUserId(999L));
    }
}
