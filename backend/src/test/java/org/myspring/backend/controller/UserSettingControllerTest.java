package org.myspring.backend.controller;

import org.junit.jupiter.api.Test;
import org.myspring.backend.dto.request.UserSettingRequest;
import org.myspring.backend.model.User;
import org.myspring.backend.model.UserPrincipal;
import org.myspring.backend.model.UserSetting;
import org.myspring.backend.service.JwtService;
import org.myspring.backend.service.UserSettingService;
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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserSettingController.class)
@Import({
        UserSettingControllerTest.AuthenticationPrincipalResolverConfig.class,
        UserSettingControllerTest.TestSecurityConfig.class
})
class UserSettingControllerTest {

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
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
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
    private UserSettingService userSettingService;

    @MockitoBean
    private JwtService jwtService;

    private static final Long USER_ID = 1L;

    private static UserPrincipal authenticatedUser() {
        User user = User.builder()
                .id(USER_ID)
                .username("testuser")
                .password("password")
                .build();

        return new UserPrincipal(user);
    }

    @Test
    void getUserSetting_returnsUserSetting() throws Exception {
        UserSetting setting = UserSetting.builder().id(5L).appTheme("dark").build();

        given(userSettingService.findByUserId(USER_ID)).willReturn(setting);

        mockMvc.perform(get("/api/user-setting").with(user(authenticatedUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.appTheme").value("dark"));
    }

    @Test
    void getUserSetting_notFound_returns404() throws Exception {
        given(userSettingService.findByUserId(USER_ID))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User Setting not found"));

        mockMvc.perform(get("/api/user-setting").with(user(authenticatedUser())))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserSetting_withoutAuthentication_isRejected() throws Exception {
        mockMvc.perform(get("/api/user-setting"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void updateUserSetting_returnsUpdatedSetting() throws Exception {
        UserSettingRequest requestBody = new UserSettingRequest("light", "sk-my-key");
        UserSetting updated = UserSetting.builder().id(5L).appTheme("light").build();

        given(userSettingService.save(eq(USER_ID), eq(requestBody))).willReturn(updated);

        mockMvc.perform(put("/api/user-setting/")
                        .with(user(authenticatedUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.appTheme").value("light"));
    }

    @Test
    void updateUserSetting_withoutAuthentication_isRejected() throws Exception {
        UserSettingRequest requestBody = new UserSettingRequest("light", null);

        mockMvc.perform(put("/api/user-setting/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().is4xxClientError());
    }
}
