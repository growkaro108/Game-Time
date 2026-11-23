package com.gametime.Backend.repository;

import com.gametime.Backend.entity.WalletTopUpRequest;
import com.gametime.Backend.entity.WalletTopUpRequest.TopUpStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletTopUpRequestRepository extends JpaRepository<WalletTopUpRequest, Long> {

    boolean existsByTransactionId(String transactionId);

    List<WalletTopUpRequest> findByUser_IdOrderByCreatedAtDesc(Long userId);

    List<WalletTopUpRequest> findByStatusOrderByCreatedAtAsc(TopUpStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select request from WalletTopUpRequest request join fetch request.user where request.id = :id")
    Optional<WalletTopUpRequest> findByIdForUpdate(@Param("id") Long id);
}
