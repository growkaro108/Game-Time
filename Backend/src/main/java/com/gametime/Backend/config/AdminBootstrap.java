package com.gametime.Backend.config;

import com.gametime.Backend.entity.User;
import com.gametime.Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL:}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (adminEmail.isBlank() && adminPassword.isBlank()) {
            return;
        }
        if (adminEmail.isBlank() || adminPassword.length() < 8) {
            throw new IllegalStateException(
                    "Set both ADMIN_EMAIL and an ADMIN_PASSWORD of at least 8 characters to bootstrap an administrator");
        }

        String normalizedEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(normalizedEmail)) {
            return;
        }

        userRepository.save(User.builder()
                .name("Game Time Admin")
                .email(normalizedEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(User.UserRole.ADMIN)
                .build());
    }
}
