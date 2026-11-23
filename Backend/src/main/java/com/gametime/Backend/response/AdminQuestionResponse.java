package com.gametime.Backend.response;

import com.gametime.Backend.entity.TournamentQuestion;

import java.util.List;

public record AdminQuestionResponse(
        Long id,
        String text,
        List<String> options,
        int correctAnswerIndex,
        int timeLimitSeconds
) {
    public static AdminQuestionResponse from(TournamentQuestion question) {
        return new AdminQuestionResponse(
                question.getId(),
                question.getText(),
                List.copyOf(question.getOptions()),
                question.getCorrectAnswerIndex(),
                question.getTimeLimitSeconds()
        );
    }
}
