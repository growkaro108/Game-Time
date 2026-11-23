package com.gametime.Backend.response;

import com.gametime.Backend.entity.WalletTopUpRequest;
import com.gametime.Backend.entity.WalletTopUpRequest.TopUpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletTopUpResponse(
        Long id,
        Long userId,
        String userName,
        String userEmail,
        BigDecimal amountInr,
        String paymentMethod,
        String transactionId,
        TopUpStatus status,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt
) {
    public static WalletTopUpResponse from(WalletTopUpRequest request) {
        return new WalletTopUpResponse(
                request.getId(),
                request.getUser().getId(),
                request.getUser().getName(),
                request.getUser().getEmail(),
                request.getAmountInr(),
                request.getPaymentMethod(),
                request.getTransactionId(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getReviewedAt()
        );
    }
}
