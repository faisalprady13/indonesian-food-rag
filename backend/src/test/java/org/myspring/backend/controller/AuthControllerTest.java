package org.myspring.backend.controller;

import org.junit.jupiter.api.Test;
import org.myspring.backend.dto.request.LoginRequest;
import org.myspring.backend.dto.request.RegisterRequest;
import org.myspring.backend.model.User;
import org.myspring.backend.model.UserPrincipal;
import org.myspring.backend.service.AuthService;
import org.myspring.backend.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({
        AuthControllerTest.AuthenticationPrincipalResolverConfig.class,
        AuthControllerTest.TestSecurityConfig.class
})
class AuthControllerTest {

    @TestConfiguration
    static class AuthenticationPrincipalResolverConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new AuthenticationPrincipalArgumentResolver());
        }
    }

    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                            .anyRequest().authenticated())
                    .exceptionHandling(exceptions -> exceptions
                            .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    private static UserPrincipal authenticatedUser() {
        User user = User.builder()
                .id(1L)
                .username("johndoe")
                .fullname("John Doe")
                .email("john@example.com")
                .provider("local")
                .password("hashed")
                .build();

        return new UserPrincipal(user);
    }

    @Test
    void getMe_returnsUserResponse_forAuthenticatedPrincipal() throws Exception {
        mockMvc.perform(get("/api/auth").with(user(authenticatedUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.fullname").value("John Doe"))
                .andExpect(jsonPath("$.provider").value("local"));
    }

    @Test
    void getMe_withoutAuthentication_isRejected() throws Exception {
        mockMvc.perform(get("/api/auth"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void login_returnsTokenFromService() throws Exception {
        LoginRequest requestBody = new LoginRequest("johndoe", "Password123!");
        given(authService.verify(requestBody)).willReturn("jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().string("jwt-token"));
    }

    @Test
    void login_returnsFail_whenServiceReportsAuthenticationFailure() throws Exception {
        LoginRequest requestBody = new LoginRequest("johndoe", "wrong-password");
        given(authService.verify(requestBody)).willReturn("fail");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().string("fail"));
    }

    @Test
    void register_returnsFieldValidationErrors_whenRequestIsInvalid() throws Exception {
        RegisterRequest requestBody = new RegisterRequest("", "not-an-email", "Chef Max", "weakpass");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("Username is required"))
                .andExpect(jsonPath("$.email").value("Email must be a valid email address"))
                .andExpect(jsonPath("$.password").value(
                        "Password must be at least 8 characters long and include an uppercase letter, a lowercase letter, a digit, and a special character"))
                .andExpect(jsonPath("$.fullname").doesNotExist());
    }

    @Test
    void register_returnsCreatedUser_whenRequestIsValid() throws Exception {
        RegisterRequest requestBody = new RegisterRequest("chef", "chef@example.com", "Chef Max", "Password123!");
        User createdUser = User.builder()
                .id(2L)
                .username("chef")
                .email("chef@example.com")
                .fullname("Chef Max")
                .provider("local")
                .build();
        given(authService.register(requestBody)).willReturn(createdUser);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.username").value("chef"))
                .andExpect(jsonPath("$.email").value("chef@example.com"))
                .andExpect(jsonPath("$.fullname").value("Chef Max"));
    }
}
