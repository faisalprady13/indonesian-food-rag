package org.myspring.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myspring.backend.exception.EmailAlreadyExistsException;
import org.myspring.backend.model.User;
import org.myspring.backend.repository.UserRepository;
import org.myspring.backend.service.UserService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomOauth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private RestClient githubRestClient;

    private CustomOauth2UserService customOauth2UserService;

    @BeforeEach
    void setUp() {
        customOauth2UserService = new CustomOauth2UserService(userRepository, userService, githubRestClient);
    }

    private static OAuth2User githubUser() {
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of(
                        "id", 12345,
                        "login", "octocat",
                        "email", "octocat@example.com",
                        "avatar_url", "https://github.com/avatar.png"
                ),
                "id"
        );
    }

    private static OAuth2User googleUser() {
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of(
                        "sub", "10987",
                        "email", "chef@gmail.com",
                        "email_verified", true,
                        "name", "Chef Max",
                        "picture", "https://google.com/picture.png"
                ),
                "sub"
        );
    }

    @Test
    void github_existingUserFoundByLogin_doesNotCreateUser() {
        when(userRepository.findByUsername("octocat")).thenReturn(Optional.of(new User()));

        customOauth2UserService.buildOAuth2User("github", githubUser(), "token");

        verify(userService, never()).createUser(any());
    }

    @Test
    void github_newUser_createdWithLoginAsUsername() {
        when(userRepository.findByUsername("octocat")).thenReturn(Optional.empty());
        when(userService.createUser(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        customOauth2UserService.buildOAuth2User("github", githubUser(), "token");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).createUser(captor.capture());
        User created = captor.getValue();

        assertThat(created.getUsername()).isEqualTo("octocat");
        assertThat(created.getEmail()).isEqualTo("octocat@example.com");
        assertThat(created.getProvider()).isEqualTo("github");
        assertThat(created.getImageUrl()).isEqualTo("https://github.com/avatar.png");
    }

    @Test
    void google_newUser_createdWithEmailAsUsernameAndPicture() {
        when(userRepository.findByUsername("chef@gmail.com")).thenReturn(Optional.empty());
        when(userService.createUser(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        customOauth2UserService.buildOAuth2User("google", googleUser(), "token");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).createUser(captor.capture());
        User created = captor.getValue();

        assertThat(created.getUsername()).isEqualTo("chef@gmail.com");
        assertThat(created.getEmail()).isEqualTo("chef@gmail.com");
        assertThat(created.getProvider()).isEqualTo("google");
        assertThat(created.getImageUrl()).isEqualTo("https://google.com/picture.png");
        assertThat(created.getFullname()).isEqualTo("Chef Max");
        verifyNoInteractions(githubRestClient);
    }

    @Test
    void google_existingUserFoundByEmail_doesNotCreateUser() {
        when(userRepository.findByUsername("chef@gmail.com")).thenReturn(Optional.of(new User()));

        customOauth2UserService.buildOAuth2User("google", googleUser(), "token");

        verify(userService, never()).createUser(any());
    }

    @Test
    void google_emailAlreadyUsedByAnotherAccount_throwsOAuth2AuthenticationException() {
        when(userRepository.findByUsername("chef@gmail.com")).thenReturn(Optional.empty());
        when(userService.createUser(any(User.class)))
                .thenThrow(new EmailAlreadyExistsException("An account with this email already exists"));

        assertThatThrownBy(() -> customOauth2UserService.buildOAuth2User("google", googleUser(), "token"))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .satisfies(e -> assertThat(((OAuth2AuthenticationException) e).getError().getErrorCode())
                        .isEqualTo("email_already_registered"));
    }

    @Test
    void returnedPrincipalNameIsAppUsername_forBothProviders() {
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(new User()));

        OAuth2User githubResult = customOauth2UserService.buildOAuth2User("github", githubUser(), "token");
        OAuth2User googleResult = customOauth2UserService.buildOAuth2User("google", googleUser(), "token");

        assertThat(githubResult.getName()).isEqualTo("octocat");
        assertThat(googleResult.getName()).isEqualTo("chef@gmail.com");
    }

    @Test
    void google_missingEmail_throwsOAuth2AuthenticationException() {
        OAuth2User googleUserWithoutEmail = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("sub", "10987", "name", "Chef Max"),
                "sub"
        );

        assertThatThrownBy(() -> customOauth2UserService.buildOAuth2User("google", googleUserWithoutEmail, "token"))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .satisfies(e -> assertThat(((OAuth2AuthenticationException) e).getError().getErrorCode())
                        .isEqualTo("email_not_found"));
        verifyNoInteractions(githubRestClient);
    }
}