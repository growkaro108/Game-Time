package com.gametime.Backend.request;

import jakarta.validation.constraints.*;

public record WalletRequest(
        @NotBlank(message = "Mode is required (deposit or withdraw)")
        String mode,

        @DecimalMin(value = "10", message = "Minimum amount is $10")
        double amount
) {}
