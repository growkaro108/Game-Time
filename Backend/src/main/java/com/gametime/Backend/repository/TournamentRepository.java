package com.gametime.Backend.repository;

import com.gametime.Backend.entity.Tournament;
import com.gametime.Backend.entity.Tournament.TournamentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    List<Tournament> findByStatus(TournamentStatus status);

    List<Tournament> findByStatusIn(List<TournamentStatus> statuses);

    @Query("SELECT t FROM Tournament t ORDER BY t.revenue DESC LIMIT 4")
    List<Tournament> findTopByRevenue();

    boolean existsByTitle(String title);
}
