package com.gametime.Backend.controllers;

import com.gametime.Backend.config.SessionAuthorization;
import com.gametime.Backend.request.CreateTournamentRequest;
import com.gametime.Backend.response.AdminQuestionResponse;
import com.gametime.Backend.response.TournamentResponse;
import com.gametime.Backend.service.TournamentService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tournaments")
@RequiredArgsConstructor
public class AdminTournamentController {

    private final TournamentService tournamentService;
    private final SessionAuthorization sessionAuthorization;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TournamentResponse create(@Valid @RequestBody CreateTournamentRequest request, HttpSession session) {
        sessionAuthorization.requireAdmin(session);
        return tournamentService.createTournament(request);
    }

    @PutMapping("/{id}")
    public TournamentResponse update(@PathVariable Long id,
                                     @Valid @RequestBody CreateTournamentRequest request,
                                     HttpSession session) {
        sessionAuthorization.requireAdmin(session);
        return tournamentService.updateTournament(id, request);
    }

    @PostMapping("/{id}/participants/{userId}")
    public TournamentResponse addParticipant(@PathVariable Long id,
                                             @PathVariable Long userId,
                                             HttpSession session) {
        sessionAuthorization.requireAdmin(session);
        return tournamentService.joinTournament(id, userId);
    }

    @GetMapping("/{id}/questions")
    public List<AdminQuestionResponse> questions(@PathVariable Long id, HttpSession session) {
        sessionAuthorization.requireAdmin(session);
        return tournamentService.getAdminTournamentQuestions(id);
    }
}
