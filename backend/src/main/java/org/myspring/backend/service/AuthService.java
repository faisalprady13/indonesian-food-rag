package org.myspring.backend.service;

import lombok.RequiredArgsConstructor;
import org.myspring.backend.dto.request.LoginRequest;
import org.myspring.backend.dto.request.RegisterRequest;
import org.myspring.backend.model.User;
import org.myspring.backend.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AuthenticationManager authManager;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public User register(RegisterRequest user) {
        User newUser = User.builder()
                .role("USER")
                .fullname(user.fullname())
                .username(user.username())
                .email(user.email())
                .provider("local")
                .password(encoder.encode(user.password()))
                .build();
        return userRepository.save(newUser);
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
