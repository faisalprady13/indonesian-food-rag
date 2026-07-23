package org.myspring.backend.controller;

import org.junit.jupiter.api.Test;
import org.myspring.backend.dto.response.RecipeResponse;
import org.myspring.backend.model.User;
import org.myspring.backend.model.UserPrincipal;
import org.myspring.backend.service.JwtService;
import org.myspring.backend.service.RecipeService;
import org.myspring.backend.service.rag.RecipeChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecipeController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(RecipeControllerTest.AuthenticationPrincipalResolverConfig.class)
class RecipeControllerTest {

    @TestConfiguration
    static class AuthenticationPrincipalResolverConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new AuthenticationPrincipalArgumentResolver());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecipeService recipeService;

    @MockitoBean
    private RecipeChatService recipeChatService;

    @MockitoBean
    private JwtService jwtService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private RequestPostProcessor withAuth() {
        Authentication auth = new TestingAuthenticationToken(
                new UserPrincipal(User
                        .builder()
                        .id(1L)
                        .username("johndoe")
                        .build()
                ), null);

        return request -> {
            SecurityContextHolder.getContext().setAuthentication(auth);
            return request;
        };
    }

    private RecipeResponse recipeResponse() {
        return new RecipeResponse(
                1L,
                "Rendang",
                "Simmer and serve.",
                LocalDateTime.now(),
                LocalDateTime.now(),
                false
        );
    }

    @Test
    void getRecipes_returnsPagedRecipes_withDefaultParams() throws Exception {
        Page<RecipeResponse> page =
                new PageImpl<>(
                        List.of(recipeResponse())
                );

        when(recipeService.getRecipes(
                eq(0),
                eq(10),
                eq("id"),
                eq("asc"),
                isNull(),
                eq(1L)
        )).thenReturn(page);

        mockMvc.perform(get("/api/recipe").with(withAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title")
                        .value("Rendang"));
    }

    @Test
    void addFavorite_returnsNoContent_onSuccess() throws Exception {
        mockMvc.perform(post("/api/recipe/2/favorite").with(withAuth()))
                .andExpect(status().isNoContent());

        verify(recipeService)
                .addFavorite(1L, 2L);
    }


    @Test
    void removeFavorite_returnsNoContent_onSuccess() throws Exception {
        mockMvc.perform(delete("/api/recipe/2/favorite").with(withAuth()))
                .andExpect(status().isNoContent());

        verify(recipeService)
                .removeFavorite(1L, 2L);
    }


    @Test
    void addFavorite_returnsNotFound_whenServiceThrows() throws Exception {
        doThrow(
                new ResponseStatusException(NOT_FOUND, "Recipe not found")
        )
                .when(recipeService)
                .addFavorite(1L, 999L);

        mockMvc.perform(post("/api/recipe/999/favorite").with(withAuth()))
                .andExpect(status().isNotFound());
    }
}
