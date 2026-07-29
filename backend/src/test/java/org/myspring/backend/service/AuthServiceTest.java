package org.myspring.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myspring.backend.dto.request.RegisterRequest;
import org.myspring.backend.model.User;
import org.springframework.security.authentication.AuthenticationManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authManager;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(jwtService, userService, authManager);
    }

    @Test
    void register_buildsLocalUserAndDelegatesCreationToUserService() {
        RegisterRequest request = new RegisterRequest("chef", "chef@example.com", "Chef Max", "password123");
        when(userService.createUser(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).createUser(userCaptor.capture());
        User createdUser = userCaptor.getValue();

        assertThat(createdUser.getUsername()).isEqualTo("chef");
        assertThat(createdUser.getEmail()).isEqualTo("chef@example.com");
        assertThat(createdUser.getFullname()).isEqualTo("Chef Max");
        assertThat(createdUser.getProvider()).isEqualTo("local");
        assertThat(createdUser.getPassword()).isNotEqualTo("password123");
        assertThat(result).isSameAs(createdUser);
    }
}
