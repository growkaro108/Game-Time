package com.gametime.Backend.repository;

import com.gametime.Backend.entity.Tournament;
import com.gametime.Backend.entity.Tournament.TournamentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    List<Tournament> findByStatus(TournamentStatus status);

    List<Tournament> findByStatusIn(List<TournamentStatus> statuses);

    @Query("SELECT t FROM Tournament t ORDER BY t.revenue DESC LIMIT 4")
    List<Tournament> findTopByRevenue();

    boolean existsByTitle(String title);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Tournament t where t.id = :id")
    Optional<Tournament> findByIdForUpdate(@Param("id") Long id);
}
