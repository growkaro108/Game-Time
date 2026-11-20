package com.gametime.Backend.service;

import com.gametime.Backend.entity.User;
import com.gametime.Backend.entity.User.UserStatus;
import com.gametime.Backend.repository.UserRepository;
import com.gametime.Backend.request.LoginRequest;
import com.gametime.Backend.request.SignupRequest;
import com.gametime.Backend.request.WalletRequest;
import com.gametime.Backend.response.UserResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // ── AUTH ─────────────────────────────────────────────────────────────────

    @Transactional
    public UserResponse signup(SignupRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        // NOTE: In production replace with BCryptPasswordEncoder
        User user = User.builder()
                .name(req.name())
                .email(req.email().toLowerCase())
                .password(req.password()) // TODO: hash password
                .build();
        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        // TODO: verify hashed password
        if (!user.getPassword().equals(req.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        if (user.getStatus() == UserStatus.BANNED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account banned");
        }
        return UserResponse.from(user);
    }

    public Map<String, Boolean> checkEmail(String email) {
        boolean available = !userRepository.existsByEmail(email.toLowerCase());
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
}
