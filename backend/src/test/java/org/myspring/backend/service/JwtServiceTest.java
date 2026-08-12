package org.myspring.backend.service;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 60_000);
    }

    private UserDetails userDetails(String username) {
        return User.withUsername(username)
                .password("password")
                .authorities("ROLE_USER")
                .build();
    }

    @Test
    void generateToken_thenExtractUserName_roundTripsTheSubject() {
        String token = jwtService.generateToken("chef");

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUserName(token)).isEqualTo("chef");
    }

    @Test
    void validateToken_returnsTrue_whenUsernameMatchesAndTokenNotExpired() {
        String token = jwtService.generateToken("chef");

        assertThat(jwtService.validateToken(token, userDetails("chef"))).isTrue();
    }

    @Test
    void validateToken_returnsFalse_whenUsernameDoesNotMatch() {
        String token = jwtService.generateToken("chef");

        assertThat(jwtService.validateToken(token, userDetails("someone-else"))).isFalse();
    }

    @Test
    void validateToken_throwsExpiredJwtException_forExpiredToken() {
        JwtService expiringJwtService = new JwtService(SECRET, -1_000);
        String expiredToken = expiringJwtService.generateToken("chef");

        assertThatThrownBy(() -> expiringJwtService.validateToken(expiredToken, userDetails("chef")))
                .isInstanceOf(ExpiredJwtException.class);
    }
}