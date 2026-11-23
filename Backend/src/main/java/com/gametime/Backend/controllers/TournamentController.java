package com.gametime.Backend.controllers;

import com.gametime.Backend.entity.Tournament.TournamentStatus;
import com.gametime.Backend.config.SessionAuthorization;
import com.gametime.Backend.entity.User;
import com.gametime.Backend.request.CreateTournamentRequest;
import com.gametime.Backend.response.PublicQuestionResponse;
import com.gametime.Backend.response.TournamentResponse;
import com.gametime.Backend.service.TournamentService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;
    private final SessionAuthorization sessionAuthorization;

    /** GET /api/tournaments  — optionally filter by status */
    @GetMapping
    public List<TournamentResponse> list(
            @RequestParam(required = false) TournamentStatus status) {
        return status != null
                ? tournamentService.getTournamentsByStatus(status)
                : tournamentService.getAllTournaments();
    }

    /** GET /api/tournaments/{id} */
    @GetMapping("/{id}")
    public TournamentResponse get(@PathVariable Long id) {
        return tournamentService.getTournamentById(id);
    }

    @GetMapping("/mine")
    public List<TournamentResponse> mine(HttpSession session) {
        User user = sessionAuthorization.requireUser(session);
        return tournamentService.getJoinedTournaments(user.getId());
    }

    @GetMapping("/{id}/questions")
    public List<PublicQuestionResponse> questions(@PathVariable Long id) {
        return tournamentService.getTournamentQuestions(id);
    }

    @PostMapping("/{id}/join")
    public TournamentResponse join(@PathVariable Long id, HttpSession session) {
        User user = sessionAuthorization.requireUser(session);
        return tournamentService.joinTournament(id, user.getId());
    }

    /** POST /api/tournaments  — admin creates a new tournament */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TournamentResponse create(@Valid @RequestBody CreateTournamentRequest req, HttpSession session) {
        sessionAuthorization.requireAdmin(session);
        return tournamentService.createTournament(req);
    }

    /** POST /api/tournaments/{id}/start */
    @PostMapping("/{id}/start")
    public TournamentResponse start(@PathVariable Long id, HttpSession session) {
        sessionAuthorization.requireAdmin(session);
        return tournamentService.startTournament(id);
    }

    /** POST /api/tournaments/{id}/end */
    @PostMapping("/{id}/end")
    public TournamentResponse end(@PathVariable Long id, HttpSession session) {
        sessionAuthorization.requireAdmin(session);
        return tournamentService.endTournament(id);
    }

    /** POST /api/tournaments/{id}/cancel */
    @PostMapping("/{id}/cancel")
    public TournamentResponse cancel(@PathVariable Long id, HttpSession session) {
        sessionAuthorization.requireAdmin(session);
        return tournamentService.cancelTournament(id);
    }
}
