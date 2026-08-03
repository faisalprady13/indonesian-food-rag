package org.myspring.backend.security;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.OAuth2UserInfo;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2UserInfoResolver {

    public static final String EMAIL_ATTRIBUTE = "email";
    public static final String PROVIDER_ATTRIBUTE = "provider";
    public static final String USERNAME_ATTRIBUTE = "username";
    public static final String IMAGE_ATTRIBUTE = "imageUrl";
    public static final String NAME_ATTRIBUTE = "name";

    private final List<OAuth2ProviderStrategy> strategies;

    public OAuth2UserInfo resolveUserInfo(String provider, OAuth2User oAuth2User, String accessToken) {
        OAuth2ProviderStrategy strategy = strategies.stream()
                .filter(s -> s.supports(provider))
                .findFirst()
                .orElseThrow(() -> new OAuth2AuthenticationException(
                        new OAuth2Error("unsupported_provider"),
                        "Unsupported OAuth2 provider: " + provider));

        String email = requireEmail(provider, strategy.resolveEmail(oAuth2User, accessToken));

        String username = strategy.resolveUsername(oAuth2User, email);
        String imageUrl = strategy.resolveImageUrl(oAuth2User);
        String fullName = strategy.resolveFullName(oAuth2User);

        return new OAuth2UserInfo(provider, email, username, imageUrl, fullName);
    }

    public Map<String, Object> toAttributeMap(OAuth2User oAuth2User, OAuth2UserInfo userInfo) {
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put(EMAIL_ATTRIBUTE, userInfo.email());
        attributes.put(PROVIDER_ATTRIBUTE, userInfo.provider());
        attributes.put(USERNAME_ATTRIBUTE, userInfo.username());
        attributes.put(IMAGE_ATTRIBUTE, userInfo.imageUrl());
        attributes.put(NAME_ATTRIBUTE, userInfo.fullName());
        return attributes;
    }

    private String requireEmail(String provider, String email) {
        if (email == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_not_found"),
                    provider + " account has no accessible verified email address");
        }
        return email;
    }
}
