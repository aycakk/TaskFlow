package com.example.taskflow.security;

import com.example.taskflow.application.service.AuthService;
import com.example.taskflow.infrastructure.persistence.entity.UserEntity;
import com.example.taskflow.infrastructure.persistence.repository.UserRepository;
import com.example.taskflow.security.dto.AuthResponse;
import com.example.taskflow.security.dto.LoginRequest;
import com.example.taskflow.security.dto.RegisterRequest;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationProvider authenticationProvider;
    private final JwtService jwtService;
    private final AuthService authService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          AuthenticationProvider authenticationProvider,
                          JwtService jwtService,
                          AuthService authService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationProvider = authenticationProvider;
        this.jwtService = jwtService;
        this.authService=authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody RegisterRequest req) {
        if (req.getUsername() == null || req.getUsername().isBlank()) {
            throw new IllegalArgumentException("username boş olamaz");
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new IllegalArgumentException("password boş olamaz");
        }

        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("username zaten kayıtlı");
        }

        UserEntity user = new UserEntity();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword())); // ✅ hash

        authService.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req) {

        Authentication auth = authenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );

        // principal = UserEntity (çünkü UserEntity implements UserDetails)
        UserEntity user = (UserEntity) auth.getPrincipal();

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }
}
