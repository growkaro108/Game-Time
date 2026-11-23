package com.gametime.Backend.service;

import com.gametime.Backend.entity.User;
import com.gametime.Backend.entity.User.UserStatus;
import com.gametime.Backend.repository.UserRepository;
import com.gametime.Backend.request.LoginRequest;
import com.gametime.Backend.request.SignupRequest;
import com.gametime.Backend.request.WalletRequest;
import com.gametime.Backend.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ── AUTH ─────────────────────────────────────────────────────────────────

    @Transactional
    public UserResponse signup(SignupRequest req) {
        String email = normalizeEmail(req.email());
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        User user = User.builder()
                .name(req.name())
                .email(email)
                .password(passwordEncoder.encode(req.password()))
                .build();
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(normalizeEmail(req.email()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordMatches(req.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not active");
        }
        if (!user.getPassword().startsWith("$2")) {
            user.setPassword(passwordEncoder.encode(req.password()));
            userRepository.save(user);
        }
        return UserResponse.from(user);
    }

    public Map<String, Boolean> checkEmail(String email) {
        boolean available = !userRepository.existsByEmail(normalizeEmail(email));
        return Map.of("available", available);
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        return UserResponse.from(findOrThrow(id));
    }

    public UserResponse getCurrentUser(Long id) {
        User user = findOrThrow(id);
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not active");
        }
        return UserResponse.from(user);
    }

    // ── ADMIN ACTIONS ────────────────────────────────────────────────────────

    @Transactional
    public UserResponse suspendUser(Long id) {
        User u = findOrThrow(id);
        u.setStatus(UserStatus.SUSPENDED);
        return UserResponse.from(userRepository.save(u));
    }

    @Transactional
    public UserResponse banUser(Long id) {
        User u = findOrThrow(id);
        u.setStatus(UserStatus.BANNED);
        return UserResponse.from(userRepository.save(u));
    }

    @Transactional
    public UserResponse reinstateUser(Long id) {
        User u = findOrThrow(id);
        u.setStatus(UserStatus.ACTIVE);
        return UserResponse.from(userRepository.save(u));
    }

    // ── WALLET ───────────────────────────────────────────────────────────────

    @Transactional
    public UserResponse walletAction(Long userId, WalletRequest req) {
        User u = findOrThrow(userId);
        BigDecimal amount = BigDecimal.valueOf(req.amount());

        switch (req.mode()) {
            case "deposit" -> u.setUsdtBalance(u.getUsdtBalance().add(amount));
            case "withdraw" -> {
                if (u.getUsdtBalance().compareTo(amount) < 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Insufficient balance");
                }
                u.setUsdtBalance(u.getUsdtBalance().subtract(amount));
                u.setPendingBalance(u.getPendingBalance().add(amount));
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Mode must be 'deposit' or 'withdraw'");
        }
        return UserResponse.from(userRepository.save(u));
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found: " + id));
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (storedPassword.startsWith("$2")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return MessageDigest.isEqual(
                rawPassword.getBytes(StandardCharsets.UTF_8),
                storedPassword.getBytes(StandardCharsets.UTF_8));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
