package com.gametime.Backend.response;

import com.gametime.Backend.entity.Tournament;
import com.gametime.Backend.entity.Tournament.TournamentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public record TournamentResponse(
        Long id,
        String title,
        String description,
        String category,
        TournamentStatus status,
        int currentPlayers,
        int maxPlayers,
        long entryFeeNexus,
        BigDecimal prizePool,
        int questionCount,
        LocalDateTime startsAt,
        LocalDateTime createdAt
) {
    public static TournamentResponse from(Tournament t) {
        Objects.requireNonNull(t, "Tournament must not be null");
        return new TournamentResponse(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getCategory(),
                t.getStatus(),
                t.getCurrentPlayers(),
                t.getMaxPlayers(),
                t.getEntryFeeNexus(),
                t.getPrizePool(),
                t.getQuestionCount(),
                t.getStartsAt(),
                t.getCreatedAt()
        );
    }
}