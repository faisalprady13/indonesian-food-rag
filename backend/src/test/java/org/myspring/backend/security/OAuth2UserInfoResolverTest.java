package org.myspring.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myspring.backend.dto.OAuth2UserInfo;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the resolver's orchestration only — strategy lookup, the "every provider must yield
 * an email" invariant, and attribute assembly. Per-provider quirks belong in each
 * {@link OAuth2ProviderStrategy} implementation's own test, e.g. {@link GithubOAuth2ProviderStrategyTest}.
 */
@ExtendWith(MockitoExtension.class)
class OAuth2UserInfoResolverTest {

    @Mock
    private OAuth2ProviderStrategy githubStrategy;

    @Mock
    private OAuth2ProviderStrategy googleStrategy;

    private OAuth2UserInfoResolver resolver;

    @BeforeEach
    void setUp() {
        // Stubbed leniently: the "unsupported provider" tests below deliberately never match it.
        lenient().when(githubStrategy.supports("github")).thenReturn(true);
        resolver = new OAuth2UserInfoResolver(List.of(githubStrategy, googleStrategy));
    }

    private static OAuth2User user(Map<String, Object> attributes) {
        return new DefaultOAuth2User(List.of(new SimpleGrantedAuthority("ROLE_USER")), attributes, "id");
    }

    private void stubGithubStrategy(OAuth2User oAuth2User) {
        when(githubStrategy.resolveEmail(oAuth2User, "token")).thenReturn("octocat@example.com");
        when(githubStrategy.resolveUsername(oAuth2User, "octocat@example.com")).thenReturn("octocat");
        when(githubStrategy.resolveImageUrl(oAuth2User)).thenReturn("https://github.com/avatar.png");
        when(githubStrategy.resolveFullName(oAuth2User)).thenReturn("The Octocat");
    }

    @Test
    void resolveUserInfo_delegatesToMatchingStrategy() {
        OAuth2User oAuth2User = user(Map.of("id", "1"));
        stubGithubStrategy(oAuth2User);

        OAuth2UserInfo userInfo = resolver.resolveUserInfo("github", oAuth2User, "token");

        assertThat(userInfo).isEqualTo(new OAuth2UserInfo(
                "github", "octocat@example.com", "octocat", "https://github.com/avatar.png", "The Octocat"));
        verifyNoInteractions(googleStrategy);
    }

    @Test
    void toAttributeMap_mergesGivenUserInfoWithoutInvokingAnyStrategy() {
        OAuth2User oAuth2User = user(Map.of("id", "1"));
        OAuth2UserInfo userInfo = new OAuth2UserInfo(
                "github", "octocat@example.com", "octocat", "https://github.com/avatar.png", "The Octocat");

        Map<String, Object> attributes = resolver.toAttributeMap(oAuth2User, userInfo);

        assertThat(attributes)
                .containsEntry("email", "octocat@example.com")
                .containsEntry("provider", "github")
                .containsEntry("username", "octocat")
                .containsEntry("imageUrl", "https://github.com/avatar.png")
                .containsEntry("name", "The Octocat")
                .containsEntry("id", "1");
        verifyNoInteractions(githubStrategy, googleStrategy);
    }

    @Test
    void unsupportedProvider_throwsUnsupportedProvider() {
        OAuth2User oAuth2User = user(Map.of("id", "1"));

        assertThatThrownBy(() -> resolver.resolveUserInfo("facebook", oAuth2User, "token"))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .satisfies(e -> assertThat(((OAuth2AuthenticationException) e).getError().getErrorCode())
                        .isEqualTo("unsupported_provider"));
    }

    @Test
    void strategyReturnsNullEmail_throwsEmailNotFound() {
        OAuth2User oAuth2User = user(Map.of("id", "1"));
        when(githubStrategy.resolveEmail(oAuth2User, "token")).thenReturn(null);

        assertThatThrownBy(() -> resolver.resolveUserInfo("github", oAuth2User, "token"))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .satisfies(e -> assertThat(((OAuth2AuthenticationException) e).getError().getErrorCode())
                        .isEqualTo("email_not_found"));
    }
}
