package com.gametime.Backend.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateWalletTopUpRequest(
        @NotNull(message = "Top-up amount is required")
        @DecimalMin(value = "1.00", message = "Minimum top-up amount is ₹1")
        @DecimalMax(value = "1000000.00", message = "Maximum top-up amount is ₹1,000,000")
        @Digits(integer = 13, fraction = 2, message = "Top-up amount can have at most 2 decimal places")
        BigDecimal amountInr,

        @NotBlank(message = "Payment method is required")
        @Size(max = 50, message = "Payment method can be at most 50 characters")
        String paymentMethod,

        @NotBlank(message = "Transaction ID is required")
        @Size(max = 150, message = "Transaction ID can be at most 150 characters")
        String transactionId
) {}
