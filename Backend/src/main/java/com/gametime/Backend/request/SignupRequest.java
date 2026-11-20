package com.gametime.Backend.request;

import jakarta.validation.constraints.*;

public record SignupRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 50, message = "Name must be 2–50 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password
) {}
