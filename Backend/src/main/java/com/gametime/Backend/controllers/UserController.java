package com.gametime.Backend.controllers;

import com.gametime.Backend.request.LoginRequest;
import com.gametime.Backend.request.SignupRequest;
import com.gametime.Backend.request.WalletRequest;
import com.gametime.Backend.response.UserResponse;
import com.gametime.Backend.config.SessionAuthorization;
import com.gametime.Backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SessionAuthorization sessionAuthorization;

    // ── AUTH endpoints (/api/auth/*) ─────────────────────────────────────────

    /** POST /api/auth/signup */
    @PostMapping("/api/auth/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse signup(@Valid @RequestBody SignupRequest req, HttpServletRequest request) {
        UserResponse user = userService.signup(req);
        establishSession(request, user.id());
        return user;
    }

    /** POST /api/auth/login */
    @PostMapping("/api/auth/login")
    public UserResponse login(@Valid @RequestBody LoginRequest req, HttpServletRequest request) {
        UserResponse user = userService.login(req);
        establishSession(request, user.id());
        return user;
    }

    @GetMapping("/api/auth/me")
    public UserResponse currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute(SessionAuthorization.USER_ID_ATTRIBUTE) instanceof Long userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in");
        }
        return userService.getCurrentUser(userId);
    }

    @PostMapping("/api/auth/logout")
    public Map<String, String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return Map.of("message", "Logged out");
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
    public List<UserResponse> listUsers(HttpSession session) {
        sessionAuthorization.requireAdmin(session);
        return userService.getAllUsers();
    }

    /** GET /api/users/{id} */
    @GetMapping("/api/users/{id}")
    public UserResponse getUser(@PathVariable Long id, HttpSession session) {
        sessionAuthorization.requireAdmin(session);
        return userService.getUserById(id);
    }

    /** POST /api/users/{id}/suspend */
    @PostMapping("/api/users/{id}/suspend")
    public UserResponse suspend(@PathVariable Long id, HttpSession session) {
        sessionAuthorization.requireAdmin(session);
        return userService.suspendUser(id);
    }

    /** POST /api/users/{id}/ban */
    @PostMapping("/api/users/{id}/ban")
    public UserResponse ban(@PathVariable Long id, HttpSession session) {
        sessionAuthorization.requireAdmin(session);
        return userService.banUser(id);
    }

    /** POST /api/users/{id}/reinstate */
    @PostMapping("/api/users/{id}/reinstate")
    public UserResponse reinstate(@PathVariable Long id, HttpSession session) {
        sessionAuthorization.requireAdmin(session);
        return userService.reinstateUser(id);
    }

    /** POST /api/users/{id}/wallet */
    @PostMapping("/api/users/{id}/wallet")
    public UserResponse wallet(@PathVariable Long id,
                               @Valid @RequestBody WalletRequest req,
                               HttpSession session) {
        sessionAuthorization.requireAdmin(session);
        return userService.walletAction(id, req);
    }

    private void establishSession(HttpServletRequest request, Long userId) {
        HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }
        request.getSession(true).setAttribute(SessionAuthorization.USER_ID_ATTRIBUTE, userId);
    }
}
