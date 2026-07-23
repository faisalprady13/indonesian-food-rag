package org.myspring.backend.controller;

import org.junit.jupiter.api.Test;
import org.myspring.backend.dto.response.ChatMessageResponse;
import org.myspring.backend.dto.response.ConversationResponse;
import org.myspring.backend.enums.MessageRole;
import org.myspring.backend.model.User;
import org.myspring.backend.model.UserPrincipal;
import org.myspring.backend.service.ConversationService;
import org.myspring.backend.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConversationController.class)
@Import({
        ConversationControllerTest.AuthenticationPrincipalResolverConfig.class,
        ConversationControllerTest.TestSecurityConfig.class
})
class ConversationControllerTest {

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

    @MockitoBean
    private ConversationService conversationService;

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

    private static ConversationResponse sampleConversation() {
        ChatMessageResponse message = new ChatMessageResponse(
                76L,
                "Hi buddy",
                MessageRole.USER,
                LocalDateTime.of(2026, 7, 23, 15, 17, 33)
        );

        return new ConversationResponse(10L, "Culinary Chat Time", List.of(message));
    }

    @Test
    void getAllConversations_returnsConversationsForAuthenticatedUser() throws Exception {
        given(conversationService.getAllConversationsByUserId(USER_ID))
                .willReturn(List.of(sampleConversation()));

        mockMvc.perform(get("/api/conversation").with(user(authenticatedUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].title").value("Culinary Chat Time"))
                .andExpect(jsonPath("$[0].messages[0].content").value("Hi buddy"))
                .andExpect(jsonPath("$[0].messages[0].role").value("USER"));
    }

    @Test
    void getAllConversations_withoutAuthentication_isRejected() throws Exception {
        mockMvc.perform(get("/api/conversation"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getDetailConversation_returnsConversation() throws Exception {
        given(conversationService.getConversationById(eq(10L), eq(USER_ID)))
                .willReturn(sampleConversation());

        mockMvc.perform(get("/api/conversation/{id}", 10L).with(user(authenticatedUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Culinary Chat Time"));
    }

    @Test
    void getDetailConversation_notFound_returns404() throws Exception {
        given(conversationService.getConversationById(eq(999L), anyLong()))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        mockMvc.perform(get("/api/conversation/{id}", 999L).with(user(authenticatedUser())))
                .andExpect(status().isNotFound());
    }
}
