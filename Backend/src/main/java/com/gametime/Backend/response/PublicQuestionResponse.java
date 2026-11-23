package com.gametime.Backend.response;

import com.gametime.Backend.entity.TournamentQuestion;

import java.util.List;

public record PublicQuestionResponse(
        Long id,
        String text,
        List<String> options,
        int timeLimitSeconds
) {
    public static PublicQuestionResponse from(TournamentQuestion question) {
        return new PublicQuestionResponse(
                question.getId(),
                question.getText(),
                List.copyOf(question.getOptions()),
                question.getTimeLimitSeconds()
        );
    }
}
