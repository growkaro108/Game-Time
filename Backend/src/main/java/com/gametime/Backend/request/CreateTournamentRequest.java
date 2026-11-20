package com.gametime.Backend.request;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record CreateTournamentRequest(
        @NotBlank(message = "Title is required")
        @Size(min = 4, max = 150, message = "Title must be 4–150 characters")
        String title,

        @NotBlank(message = "Category is required")
        String category,

        @NotNull(message = "Prize pool is required")
        @DecimalMin(value = "1", message = "Prize pool must be at least 1")
        BigDecimal prizePool,

        @Min(value = 0, message = "Entry fee cannot be negative")
        long entryFeeNexus,

        @Min(value = 2, message = "Capacity must be at least 2")
        int maxPlayers,

        @NotNull(message = "Start time is required")
        @Future(message = "Start time must be in the future")
        LocalDateTime startsAt,

        @Min(value = 5, message = "Questions must be at least 5")
        @Max(value = 100, message = "Questions must be at most 100")
        int questionCount,

        @Size(max = 500, message = "Description can be at most 500 characters")
        String description
) {}
