package com.gametime.Backend.service;

import com.gametime.Backend.entity.Tournament;
import com.gametime.Backend.entity.Tournament.TournamentStatus;
import com.gametime.Backend.repository.TournamentRepository;
import com.gametime.Backend.request.CreateTournamentRequest;
import com.gametime.Backend.response.TournamentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentService {

    private final TournamentRepository tournamentRepository;

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

    public List<TournamentResponse> getTournamentsByStatus(TournamentStatus status) {
        return tournamentRepository.findByStatus(status)
                .stream()
                .map(TournamentResponse::from)
                .toList();
    }

    // ── CREATE ───────────────────────────────────────────────────────────────

    @Transactional
    public TournamentResponse createTournament(CreateTournamentRequest req) {
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
        return TournamentResponse.from(tournamentRepository.save(tournament));
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
