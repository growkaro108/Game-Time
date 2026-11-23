package com.gametime.Backend.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateQuestionRequest(
        @NotBlank(message = "Question text is required")
        @Size(max = 1000, message = "Question text can be at most 1000 characters")
        String text,

        @NotNull(message = "Question options are required")
        @Size(min = 2, max = 6, message = "Each question must have 2 to 6 options")
        List<@NotBlank(message = "Question options cannot be blank")
                @Size(max = 500, message = "An option can be at most 500 characters") String> options,

        @Min(value = 0, message = "Correct answer index cannot be negative")
        @Max(value = 5, message = "Correct answer index must be at most 5")
        int correctAnswerIndex,

        @Min(value = 5, message = "Question time limit must be at least 5 seconds")
        @Max(value = 60, message = "Question time limit cannot exceed 60 seconds")
        int timeLimitSeconds
) {}
