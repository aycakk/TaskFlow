package com.example.taskflow.application.service;

import com.example.taskflow.infrastructure.persistence.entity.UserEntity;
import com.example.taskflow.infrastructure.persistence.repository.UserRepository;
import com.example.taskflow.security.dto.RegisterRequest;
import com.example.taskflow.security.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequest request) {
        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());

        // 🔴 KRİTİK SATIR
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);
    }
}
