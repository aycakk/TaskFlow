package com.example.taskflow.security;

import com.example.taskflow.infrastructure.persistence.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DBUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public DBUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public com.example.taskflow.infrastructure.persistence.entity.UserEntity loadUserByUsername(String username)
            throws UsernameNotFoundException {

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
