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
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private static final String EMAIL_ATTRIBUTE = "email";
    private static final String LOGIN_ATTRIBUTE = "login";
    private static final String PROVIDER_ATTRIBUTE = "provider";
    private static final String IMAGE_ATTRIBUTE = "avatar_url";

    private final UserRepository userRepository;
    private final RestClient githubRestClient;

    @Override
    @NonNull
    public OAuth2User loadUser(@NonNull OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = resolveEmail(oAuth2User, userRequest);
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put(EMAIL_ATTRIBUTE, email);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        attributes.put(PROVIDER_ATTRIBUTE, provider);

        userRepository.findByUsername(oAuth2User.getAttribute(LOGIN_ATTRIBUTE))
                .orElseGet(() -> createUser(attributes));

        String userNameAttributeName =
                userRequest.getClientRegistration()
                        .getProviderDetails()
                        .getUserInfoEndpoint()
                        .getUserNameAttributeName();


        if (userNameAttributeName == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_user_info_response"),
                    "No user name attribute configured for OAuth2 provider."
            );
        }

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                attributes,
                userNameAttributeName
        );
    }

    private User createUser(Map<String, Object> attributes) {
        User user = User.builder()
                .username(attributes.get(LOGIN_ATTRIBUTE).toString())
                .email(attributes.get(EMAIL_ATTRIBUTE).toString())
                .provider(attributes.get(PROVIDER_ATTRIBUTE).toString())
                .imageUrl(attributes.get(IMAGE_ATTRIBUTE).toString())
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
