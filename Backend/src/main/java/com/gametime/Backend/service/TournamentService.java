package com.gametime.Backend.service;

import com.gametime.Backend.entity.Tournament;
import com.gametime.Backend.entity.Tournament.TournamentStatus;
import com.gametime.Backend.entity.TournamentQuestion;
import com.gametime.Backend.entity.User;
import com.gametime.Backend.repository.TournamentRepository;
import com.gametime.Backend.repository.UserRepository;
import com.gametime.Backend.request.CreateQuestionRequest;
import com.gametime.Backend.request.CreateTournamentRequest;
import com.gametime.Backend.response.AdminQuestionResponse;
import com.gametime.Backend.response.PublicQuestionResponse;
import com.gametime.Backend.response.TournamentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final UserRepository userRepository;

    // ── READ ─────────────────────────────────────────────────────────────────

    public List<TournamentResponse> getAllTournaments() {
        return tournamentRepository.findAll()
                .stream()
                .map(TournamentResponse::from)
                .toList();
    }

    public TournamentResponse getTournamentById(Long id) {
        return TournamentResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<PublicQuestionResponse> getTournamentQuestions(Long id) {
        return findOrThrow(id).getQuestions().stream()
                .map(PublicQuestionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminQuestionResponse> getAdminTournamentQuestions(Long id) {
        return findOrThrow(id).getQuestions().stream()
                .map(AdminQuestionResponse::from)
                .toList();
    }

    @Transactional
    public TournamentResponse updateTournament(Long id, CreateTournamentRequest req) {
        Tournament tournament = findOrThrow(id);
        if (tournament.getStatus() != TournamentStatus.SCHEDULED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only scheduled tournaments can be edited");
        }
        if (tournament.getCurrentPlayers() > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Tournaments cannot be edited after users have joined");
        }
        validateQuestions(req);
        tournament.setTitle(req.title());
        tournament.setCategory(req.category());
        tournament.setPrizePool(req.prizePool());
        tournament.setEntryFeeNexus(req.entryFeeNexus());
        tournament.setMaxPlayers(req.maxPlayers());
        tournament.setStartsAt(req.startsAt());
        tournament.setQuestionCount(req.questionCount());
        tournament.setDescription(req.description());
        tournament.getQuestions().clear();
        tournament.getQuestions().addAll(buildQuestions(tournament, req));
        return TournamentResponse.from(tournamentRepository.save(tournament));
    }

    @Transactional
    public TournamentResponse joinTournament(Long tournamentId, Long userId) {
        Tournament tournament = tournamentRepository.findByIdForUpdate(tournamentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Tournament not found: " + tournamentId));
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only active users can join a tournament");
        }
        if (tournament.getStatus() != TournamentStatus.SCHEDULED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only scheduled tournaments can be joined");
        }
        if (tournament.getCurrentPlayers() >= tournament.getMaxPlayers()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tournament is full");
        }
        if (user.getJoinedTournaments().stream().anyMatch(joined -> joined.getId().equals(tournamentId))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already joined this tournament");
        }

        BigDecimal entryFeeInr = BigDecimal.valueOf(tournament.getEntryFeeNexus());
        if (user.getInrBalance().compareTo(entryFeeInr) < 0) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,
                    "Not enough INR wallet balance");
        }
        user.setInrBalance(user.getInrBalance().subtract(entryFeeInr));
        user.getJoinedTournaments().add(tournament);
        tournament.setCurrentPlayers(tournament.getCurrentPlayers() + 1);
        tournament.getRegisteredUsers().add(user);
        userRepository.save(user);
        return TournamentResponse.from(tournamentRepository.save(tournament));
    }

    @Transactional(readOnly = true)
    public List<TournamentResponse> getJoinedTournaments(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return user.getJoinedTournaments().stream()
                .map(TournamentResponse::from)
                .toList();
    }

    public List<TournamentResponse> getTournamentsByStatus(TournamentStatus status) {
        return tournamentRepository.findByStatus(status)
                .stream()
                .map(TournamentResponse::from)
                .toList();
    }

    // ── CREATE ───────────────────────────────────────────────────────────────

    @Transactional
    public TournamentResponse createTournament(CreateTournamentRequest req) {
        validateQuestions(req);
        Tournament tournament = Tournament.builder()
                .title(req.title())
                .category(req.category())
                .prizePool(req.prizePool())
                .entryFeeNexus(req.entryFeeNexus())
                .maxPlayers(req.maxPlayers())
                .startsAt(req.startsAt())
                .questionCount(req.questionCount())
                .description(req.description())
                .status(TournamentStatus.SCHEDULED)
                .build();
        tournament.setQuestions(buildQuestions(tournament, req));
        return TournamentResponse.from(tournamentRepository.save(tournament));
    }

    private void validateQuestions(CreateTournamentRequest req) {
        if (req.questions().size() != req.questionCount()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Question count must match the number of submitted questions");
        }
        for (CreateQuestionRequest question : req.questions()) {
            if (question.correctAnswerIndex() >= question.options().size()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Correct answer index must refer to an option");
            }
        }
    }

    private List<TournamentQuestion> buildQuestions(Tournament tournament, CreateTournamentRequest req) {
        return req.questions().stream()
                .map(question -> TournamentQuestion.builder()
                        .tournament(tournament)
                        .text(question.text().trim())
                        .options(question.options().stream().map(String::trim).toList())
                        .correctAnswerIndex(question.correctAnswerIndex())
                        .timeLimitSeconds(question.timeLimitSeconds())
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // ── LIFECYCLE ACTIONS ────────────────────────────────────────────────────

    @Transactional
    public TournamentResponse startTournament(Long id) {
        Tournament t = findOrThrow(id);
        if (t.getStatus() != TournamentStatus.SCHEDULED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only scheduled tournaments can be started");
        }
        t.setStatus(TournamentStatus.LIVE);
        return TournamentResponse.from(tournamentRepository.save(t));
    }

    @Transactional
    public TournamentResponse endTournament(Long id) {
        Tournament t = findOrThrow(id);
        if (t.getStatus() != TournamentStatus.LIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only live tournaments can be ended");
        }
        t.setStatus(TournamentStatus.COMPLETED);
        return TournamentResponse.from(tournamentRepository.save(t));
    }

    @Transactional
    public TournamentResponse cancelTournament(Long id) {
        Tournament t = findOrThrow(id);
        if (t.getStatus() == TournamentStatus.COMPLETED || t.getStatus() == TournamentStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Tournament is already finished");
        }
        t.setStatus(TournamentStatus.CANCELLED);
        return TournamentResponse.from(tournamentRepository.save(t));
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    private Tournament findOrThrow(Long id) {
        return tournamentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Tournament not found: " + id));
    }
}
