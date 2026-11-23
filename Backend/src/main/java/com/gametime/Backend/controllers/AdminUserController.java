package com.gametime.Backend.controllers;

import com.gametime.Backend.config.SessionAuthorization;
import com.gametime.Backend.request.SignupRequest;
import com.gametime.Backend.response.UserResponse;
import com.gametime.Backend.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final SessionAuthorization sessionAuthorization;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody SignupRequest request, HttpSession session) {
        sessionAuthorization.requireAdmin(session);
        return userService.signup(request);
    }
}
