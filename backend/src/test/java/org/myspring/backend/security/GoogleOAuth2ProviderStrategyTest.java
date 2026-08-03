package org.myspring.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoogleOAuth2ProviderStrategyTest {

    private GoogleOAuth2ProviderStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new GoogleOAuth2ProviderStrategy();
    }

    private static OAuth2User user(Map<String, Object> attributes) {
        return new DefaultOAuth2User(List.of(new SimpleGrantedAuthority("ROLE_USER")), attributes, "sub");
    }

    @Test
    void supports_google_isTrue() {
        assertThat(strategy.supports("google")).isTrue();
    }

    @Test
    void supports_otherProvider_isFalse() {
        assertThat(strategy.supports("github")).isFalse();
    }

    @Test
    void resolveUsername_returnsEmail() {
        OAuth2User user = user(Map.of("sub", "1"));

        assertThat(strategy.resolveUsername(user, "chef@gmail.com")).isEqualTo("chef@gmail.com");
    }

    @Test
    void resolveImageUrl_returnsPicture() {
        OAuth2User user = user(Map.of("sub", "1", "picture", "https://google.com/picture.png"));

        assertThat(strategy.resolveImageUrl(user)).isEqualTo("https://google.com/picture.png");
    }

    @Test
    void resolveFullName_returnsName() {
        OAuth2User user = user(Map.of("sub", "1", "name", "Chef Max"));

        assertThat(strategy.resolveFullName(user)).isEqualTo("Chef Max");
    }

    @Test
    void resolveFullName_missingName_returnsNull() {
        OAuth2User user = user(Map.of("sub", "1"));

        assertThat(strategy.resolveFullName(user)).isNull();
    }

    @Test
    void resolveEmail_missingEmail_returnsNull() {
        OAuth2User user = user(Map.of("sub", "1", "name", "Chef Max"));

        assertThat(strategy.resolveEmail(user, "token")).isNull();
    }

    @Test
    void resolveEmail_unverifiedEmail_throwsEmailNotVerified() {
        OAuth2User user = user(Map.of("sub", "1", "email", "chef@gmail.com", "email_verified", false));

        assertThatThrownBy(() -> strategy.resolveEmail(user, "token"))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .satisfies(e -> assertThat(((OAuth2AuthenticationException) e).getError().getErrorCode())
                        .isEqualTo("email_not_verified"));
    }

    @Test
    void resolveEmail_verifiedEmail_returnsEmail() {
        OAuth2User user = user(Map.of("sub", "1", "email", "chef@gmail.com", "email_verified", true));

        assertThat(strategy.resolveEmail(user, "token")).isEqualTo("chef@gmail.com");
    }
}
