package org.myspring.backend.security;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.myspring.backend.exception.EmailAlreadyExistsException;
import org.myspring.backend.model.User;
import org.myspring.backend.repository.UserRepository;
import org.myspring.backend.service.UserService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private static final String EMAIL_ATTRIBUTE = "email";
    private static final String EMAIL_VERIFIED_ATTRIBUTE = "email_verified";
    private static final String LOGIN_ATTRIBUTE = "login";
    private static final String NAME_ATTRIBUTE = "name";
    private static final String PROVIDER_ATTRIBUTE = "provider";
    private static final String AVATAR_URL_ATTRIBUTE = "avatar_url";
    private static final String PICTURE_ATTRIBUTE = "picture";

    // Synthetic keys we inject ourselves; neither GitHub's nor Google's userinfo payload contains them.
    private static final String USERNAME_ATTRIBUTE = "username";
    private static final String IMAGE_ATTRIBUTE = "imageUrl";

    private static final String GITHUB_PROVIDER = "github";
    private static final String GOOGLE_PROVIDER = "google";

    private final UserRepository userRepository;
    private final UserService userService;
    private final RestClient githubRestClient;

    @Override
    @NonNull
    public OAuth2User loadUser(@NonNull OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String accessToken = userRequest.getAccessToken().getTokenValue();

        return buildOAuth2User(provider, oAuth2User, accessToken);
    }

    public OAuth2User buildOAuth2User(String provider, OAuth2User oAuth2User, String accessToken) {
        String email = resolveEmail(provider, oAuth2User, accessToken);
        String username = resolveUsername(provider, oAuth2User, email);

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put(EMAIL_ATTRIBUTE, email);
        attributes.put(PROVIDER_ATTRIBUTE, provider);
        attributes.put(USERNAME_ATTRIBUTE, username);
        attributes.put(IMAGE_ATTRIBUTE, resolveImageUrl(provider, oAuth2User));

        userRepository.findByUsername(username)
                .orElseGet(() -> createUser(attributes));

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                attributes,
                USERNAME_ATTRIBUTE
        );
    }

    private User createUser(Map<String, Object> attributes) {
        User newUser = User.builder()
                .username(attributes.get(USERNAME_ATTRIBUTE).toString())
                .email(attributes.get(EMAIL_ATTRIBUTE).toString())
                .provider(attributes.get(PROVIDER_ATTRIBUTE).toString())
                .imageUrl(Objects.toString(attributes.get(IMAGE_ATTRIBUTE), null))
                .fullname(Objects.toString(attributes.get(NAME_ATTRIBUTE), null))
                .build();

        try {
            return userService.createUser(newUser);
        } catch (EmailAlreadyExistsException | DataIntegrityViolationException e) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_already_registered"),
                    "An account with this email already exists", e);
        }
    }

    private String resolveUsername(String provider, OAuth2User oAuth2User, String email) {
        return switch (provider) {
            case GITHUB_PROVIDER -> oAuth2User.getAttribute(LOGIN_ATTRIBUTE);
            case GOOGLE_PROVIDER -> email;
            default -> throw unsupportedProvider(provider);
        };
    }

    private String resolveImageUrl(String provider, OAuth2User oAuth2User) {
        return switch (provider) {
            case GITHUB_PROVIDER -> oAuth2User.getAttribute(AVATAR_URL_ATTRIBUTE);
            case GOOGLE_PROVIDER -> oAuth2User.getAttribute(PICTURE_ATTRIBUTE);
            default -> throw unsupportedProvider(provider);
        };
    }

    private String resolveEmail(String provider, OAuth2User oAuth2User, String accessToken) {
        String email = oAuth2User.getAttribute(EMAIL_ATTRIBUTE);

        if (email == null && GITHUB_PROVIDER.equals(provider)) {
            email = fetchPrimaryVerifiedEmail(accessToken);
        }

        if (email == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_not_found"),
                    provider + " account has no accessible verified email address");
        }

        if (GOOGLE_PROVIDER.equals(provider) && Boolean.FALSE.equals(oAuth2User.getAttribute(EMAIL_VERIFIED_ATTRIBUTE))) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_not_verified"),
                    "Google account email is not verified");
        }

        return email;
    }

    private OAuth2AuthenticationException unsupportedProvider(String provider) {
        return new OAuth2AuthenticationException(
                new OAuth2Error("unsupported_provider"),
                "Unsupported OAuth2 provider: " + provider);
    }

    private String fetchPrimaryVerifiedEmail(String accessToken) {
        List<Map<String, Object>> emails = githubRestClient.get()
                .uri("/user/emails")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                }); // use generic type ParameterizedTypeReference

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