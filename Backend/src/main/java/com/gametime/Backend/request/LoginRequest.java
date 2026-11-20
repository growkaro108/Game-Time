package com.gametime.Backend.request;

import jakarta.validation.constraints.*;

public record LoginRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        String password,

        boolean remember
) {}
