package org.myspring.backend.security;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.myspring.backend.dto.OAuth2UserInfo;
import org.myspring.backend.exception.EmailAlreadyExistsException;
import org.myspring.backend.model.User;
import org.myspring.backend.repository.UserRepository;
import org.myspring.backend.service.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

import static org.myspring.backend.security.OAuth2UserInfoResolver.USERNAME_ATTRIBUTE;

@Service
@RequiredArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final OAuth2UserInfoResolver userInfoResolver;

    @Override
    @NonNull
    public OAuth2User loadUser(@NonNull OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String accessToken = userRequest.getAccessToken().getTokenValue();

        return buildOAuth2User(provider, oAuth2User, accessToken);
    }

    public OAuth2User buildOAuth2User(String provider, OAuth2User oAuth2User, String accessToken) {
        OAuth2UserInfo userInfo = userInfoResolver.resolveUserInfo(provider, oAuth2User, accessToken);

        userRepository.findByUsername(userInfo.username())
                .orElseGet(() -> createUser(userInfo));

        Map<String, Object> attributes = userInfoResolver.toAttributeMap(oAuth2User, userInfo);

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                attributes,
                USERNAME_ATTRIBUTE
        );
    }

    private User createUser(OAuth2UserInfo userInfo) {
        User newUser = User.builder()
                .username(userInfo.username())
                .email(userInfo.email())
                .provider(userInfo.provider())
                .imageUrl(userInfo.imageUrl())
                .fullname(userInfo.fullName())
                .build();

        try {
            return userService.createUser(newUser);
        } catch (EmailAlreadyExistsException | DataIntegrityViolationException e) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_already_registered"),
                    "An account with this email already exists", e);
        }
    }
}
