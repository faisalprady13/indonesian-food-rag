package org.myspring.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myspring.backend.dto.request.RegisterRequest;
import org.myspring.backend.model.User;
import org.myspring.backend.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authManager;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(jwtService, userRepository, authManager);
    }

    @Test
    void register_attachesDefaultDarkThemeUserSetting() {
        RegisterRequest request = new RegisterRequest("chef", "chef@example.com", "Chef Max", "password123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.register(request);

        assertThat(result.getUsername()).isEqualTo("chef");
        assertThat(result.getUserSetting()).isNotNull();
        assertThat(result.getUserSetting().getAppTheme()).isEqualTo("dark");
        assertThat(result.getUserSetting().getUser()).isEqualTo(result);
    }
}
