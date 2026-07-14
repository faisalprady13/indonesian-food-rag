package org.myspring.backend.security;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.myspring.backend.model.User;
import org.myspring.backend.repository.UserRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private static final String EMAIL_ATTRIBUTE = "email";
    private final UserRepository userRepository;
    private final RestClient githubRestClient;

    @Override
    @NonNull
    public OAuth2User loadUser(@NonNull OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = resolveEmail(oAuth2User, userRequest);
        oAuth2User.getAttributes().put(EMAIL_ATTRIBUTE, email);

        userRepository.findByUsername(oAuth2User.getName())
                .orElseGet(() -> createUser(oAuth2User));

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                oAuth2User.getAttributes(),
                "id"
        );
    }

    private User createUser(OAuth2User oAuth2User) {
        User user = User.builder()
                .username(Objects.requireNonNull(oAuth2User.getAttribute("login")))
                .email(Objects.requireNonNull(oAuth2User.getAttribute(EMAIL_ATTRIBUTE)))
                .role("USER")
                .build();

        userRepository.save(user);
        return user;
    }

    private String resolveEmail(OAuth2User oAuth2User, OAuth2UserRequest userRequest) {
        String email = oAuth2User.getAttribute(EMAIL_ATTRIBUTE);
        if (email == null) {
            email = fetchPrimaryVerifiedEmail(userRequest.getAccessToken().getTokenValue());
        }
        if (email == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_not_found"),
                    "GitHub account has no accessible verified email address");
        }

        return email;
    }

    private String fetchPrimaryVerifiedEmail(String accessToken) {
        List<Map<String, Object>> emails = githubRestClient.get()
                .uri("/user/emails")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {}); // use generic type ParameterizedTypeReference

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
