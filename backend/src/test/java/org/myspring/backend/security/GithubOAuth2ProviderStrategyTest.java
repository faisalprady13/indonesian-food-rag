package org.myspring.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GithubOAuth2ProviderStrategyTest {

    @Mock
    private RestClient githubRestClient;

    private GithubOAuth2ProviderStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new GithubOAuth2ProviderStrategy(githubRestClient);
    }

    private static OAuth2User user(Map<String, Object> attributes) {
        return new DefaultOAuth2User(List.of(new SimpleGrantedAuthority("ROLE_USER")), attributes, "id");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void stubEmailsEndpoint(List<Map<String, Object>> emails) {
        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(githubRestClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri("/user/emails")).thenReturn(headersSpec);
        when(headersSpec.header(eq("Authorization"), any(String.class))).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(emails);
    }

    @Test
    void supports_github_isTrue() {
        assertThat(strategy.supports("github")).isTrue();
    }

    @Test
    void supports_otherProvider_isFalse() {
        assertThat(strategy.supports("google")).isFalse();
    }

    @Test
    void resolveUsername_returnsLogin() {
        OAuth2User user = user(Map.of("id", 1, "login", "octocat"));

        assertThat(strategy.resolveUsername(user, "octocat@example.com")).isEqualTo("octocat");
    }

    @Test
    void resolveFullName_usesNameAttributeWhenPresent() {
        OAuth2User user = user(Map.of("id", 1, "login", "octocat", "name", "The Octocat"));

        assertThat(strategy.resolveFullName(user)).isEqualTo("The Octocat");
    }

    @Test
    void resolveFullName_fallsBackToLoginWhenNameMissing() {
        OAuth2User user = user(Map.of("id", 1, "login", "octocat"));

        assertThat(strategy.resolveFullName(user)).isEqualTo("octocat");
    }

    @Test
    void resolveImageUrl_returnsAvatarUrl() {
        OAuth2User user = user(Map.of("id", 1, "avatar_url", "https://github.com/avatar.png"));

        assertThat(strategy.resolveImageUrl(user)).isEqualTo("https://github.com/avatar.png");
    }

    @Test
    void resolveEmail_usesEmailAttributeWhenPresent() {
        OAuth2User user = user(Map.of("id", 1, "email", "octocat@example.com"));

        assertThat(strategy.resolveEmail(user, "token")).isEqualTo("octocat@example.com");
    }

    @Test
    void resolveEmail_fallsBackToPrimaryVerifiedEmailFromApi() {
        OAuth2User user = user(Map.of("id", 1));
        stubEmailsEndpoint(List.of(
                Map.of("email", "secondary@example.com", "primary", false, "verified", true),
                Map.of("email", "octocat@example.com", "primary", true, "verified", true)
        ));

        assertThat(strategy.resolveEmail(user, "token")).isEqualTo("octocat@example.com");
    }

    @Test
    void resolveEmail_noVerifiedPrimaryEmail_returnsNull() {
        OAuth2User user = user(Map.of("id", 1));
        stubEmailsEndpoint(List.of(
                Map.of("email", "octocat@example.com", "primary", true, "verified", false)
        ));

        assertThat(strategy.resolveEmail(user, "token")).isNull();
    }

    @Test
    void resolveEmail_emptyResponseBody_returnsNull() {
        OAuth2User user = user(Map.of("id", 1));
        stubEmailsEndpoint(null);

        assertThat(strategy.resolveEmail(user, "token")).isNull();
    }
}
