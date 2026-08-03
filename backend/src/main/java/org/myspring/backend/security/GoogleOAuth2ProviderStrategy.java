package org.myspring.backend.security;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class GoogleOAuth2ProviderStrategy implements OAuth2ProviderStrategy {

    private static final String PROVIDER = "google";

    private static final String EMAIL_ATTRIBUTE = "email";
    private static final String EMAIL_VERIFIED_ATTRIBUTE = "email_verified";
    private static final String PICTURE_ATTRIBUTE = "picture";

    @Override
    public boolean supports(String provider) {
        return PROVIDER.equals(provider);
    }

    @Override
    public String resolveEmail(OAuth2User oAuth2User, String accessToken) {
        String email = oAuth2User.getAttribute(EMAIL_ATTRIBUTE);

        if (email != null && Boolean.FALSE.equals(oAuth2User.getAttribute(EMAIL_VERIFIED_ATTRIBUTE))) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_not_verified"),
                    "Google account email is not verified");
        }

        return email;
    }

    @Override
    public String resolveUsername(OAuth2User oAuth2User, String email) {
        return email;
    }

    @Override
    public String resolveImageUrl(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute(PICTURE_ATTRIBUTE);
    }

    @Override
    public String resolveFullName(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute("name");
    }
}
