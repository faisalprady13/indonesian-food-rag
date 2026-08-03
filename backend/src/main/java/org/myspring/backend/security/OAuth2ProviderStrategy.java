package org.myspring.backend.security;

import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2ProviderStrategy {

    boolean supports(String provider);

    String resolveEmail(OAuth2User oAuth2User, String accessToken);

    String resolveUsername(OAuth2User oAuth2User);

    String resolveImageUrl(OAuth2User oAuth2User);

    String resolveFullName(OAuth2User oAuth2User);
}
