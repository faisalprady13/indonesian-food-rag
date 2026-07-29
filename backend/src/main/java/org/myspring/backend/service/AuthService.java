package org.myspring.backend.service;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.request.LoginRequest;
import org.myspring.backend.dto.request.RegisterRequest;
import org.myspring.backend.model.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtService jwtService;
    private final UserService userService;
    private final AuthenticationManager authManager;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Transactional
    public User register(RegisterRequest user) {
        User newUser = User.builder()
                .fullname(user.fullname())
                .username(user.username())
                .email(user.email())
                .provider("local")
                .password(encoder.encode(user.password()))
                .build();

        return userService.createUser(newUser);
    }

    public String verify(LoginRequest user) {
        Authentication authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(user.username(), user.password()));
        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(user.username());
        } else {
            return "fail";
        }
    }
}
