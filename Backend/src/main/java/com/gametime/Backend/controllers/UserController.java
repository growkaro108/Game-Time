package com.gametime.Backend.controllers;

import com.gametime.Backend.request.LoginRequest;
import com.gametime.Backend.request.SignupRequest;
import com.gametime.Backend.request.WalletRequest;
import com.gametime.Backend.response.UserResponse;
import com.gametime.Backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ── AUTH endpoints (/api/auth/*) ─────────────────────────────────────────

    /** POST /api/auth/signup */
    @PostMapping("/api/auth/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse signup(@Valid @RequestBody SignupRequest req) {
        return userService.signup(req);
    }

    /** POST /api/auth/login */
    @PostMapping("/api/auth/login")
    public UserResponse login(@Valid @RequestBody LoginRequest req) {
        return userService.login(req);
    }

    /** GET /api/auth/check-email?email=... */
    @GetMapping("/api/auth/check-email")
    public Map<String, Boolean> checkEmail(@RequestParam String email) {
        return userService.checkEmail(email);
    }

    /** POST /api/auth/forgot-password  (placeholder — returns success) */
    @PostMapping("/api/auth/forgot-password")
    public Map<String, String> forgotPassword(@RequestBody Map<String, String> body) {
        // TODO: integrate email service (JavaMailSender / SendGrid)
        return Map.of("message", "If that email exists, a reset link has been sent.");
    }

    // ── USER endpoints (/api/users/*) ────────────────────────────────────────

    /** GET /api/users */
    @GetMapping("/api/users")
    public List<UserResponse> listUsers() {
        return userService.getAllUsers();
    }

    /** GET /api/users/{id} */
    @GetMapping("/api/users/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    /** POST /api/users/{id}/suspend */
    @PostMapping("/api/users/{id}/suspend")
    public UserResponse suspend(@PathVariable Long id) {
        return userService.suspendUser(id);
    }

    /** POST /api/users/{id}/ban */
    @PostMapping("/api/users/{id}/ban")
    public UserResponse ban(@PathVariable Long id) {
        return userService.banUser(id);
    }

    /** POST /api/users/{id}/reinstate */
    @PostMapping("/api/users/{id}/reinstate")
    public UserResponse reinstate(@PathVariable Long id) {
        return userService.reinstateUser(id);
    }

    /** POST /api/users/{id}/wallet */
    @PostMapping("/api/users/{id}/wallet")
    public UserResponse wallet(@PathVariable Long id,
                               @Valid @RequestBody WalletRequest req) {
        return userService.walletAction(id, req);
    }
}
