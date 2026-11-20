package com.gametime.Backend.response;

import com.gametime.Backend.entity.User;
import com.gametime.Backend.entity.User.Tier;
import com.gametime.Backend.entity.User.UserStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public record UserResponse(
        Long id,
        String name,
        String email,
        UserStatus status,
        User.UserRole role,
        Tier tier,
        int level,
        int ratingPoints,
        int streak,
        int globalRank,
        BigDecimal usdtBalance,
        long nexusTokens,
        BigDecimal pendingBalance,
        String country,
        LocalDateTime createdAt
) {
    public static UserResponse from(User u) {
        Objects.requireNonNull(u, "User must not be null");
        return new UserResponse(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getStatus(),
                u.getRole(),
                u.getTier(),
                u.getLevel(),
                u.getRatingPoints(),
                u.getStreak(),
                u.getGlobalRank(),
                u.getUsdtBalance(),
                u.getNexusTokens(),
                u.getPendingBalance(),
                u.getCountry(),
                u.getCreatedAt()
        );
    }
}