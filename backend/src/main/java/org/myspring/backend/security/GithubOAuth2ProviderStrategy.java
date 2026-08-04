package org.myspring.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GithubOAuth2ProviderStrategy implements OAuth2ProviderStrategy {

    private static final String PROVIDER = "github";

    private static final String EMAIL_ATTRIBUTE = "email";
    private static final String LOGIN_ATTRIBUTE = "login";
    private static final String AVATAR_URL_ATTRIBUTE = "avatar_url";

    private final RestClient githubRestClient;

    @Override
    public boolean supports(String provider) {
        return PROVIDER.equals(provider);
    }

    @Override
    public String resolveEmail(OAuth2User oAuth2User, String accessToken) {
        String email = oAuth2User.getAttribute(EMAIL_ATTRIBUTE);
        // GitHub omits the email attribute when the account's email is set to private;
        // fall back to the dedicated emails endpoint to find a verified primary one.
        return email != null ? email : fetchPrimaryVerifiedEmail(accessToken);
    }

    @Override
    public String resolveUsername(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute(LOGIN_ATTRIBUTE);
    }

    @Override
    public String resolveImageUrl(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute(AVATAR_URL_ATTRIBUTE);
    }

    @Override
    public String resolveFullName(OAuth2User oAuth2User) {
        String name = oAuth2User.getAttribute("name");
        return (name != null) ? name : oAuth2User.getAttribute("login");
    }

    private String fetchPrimaryVerifiedEmail(String accessToken) {
        List<Map<String, Object>> emails = githubRestClient.get()
                .uri("/user/emails")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        if (emails == null) {
            return null;
        }

        return emails.stream()
                .filter(e -> Boolean.TRUE.equals(e.get("primary")) && Boolean.TRUE.equals(e.get("verified")))
                .map(e -> (String) e.get(EMAIL_ATTRIBUTE))
                .findFirst()
                .orElse(null);
    }
}
