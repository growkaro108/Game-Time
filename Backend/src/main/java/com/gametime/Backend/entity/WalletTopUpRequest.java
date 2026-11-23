package com.gametime.Backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_top_up_requests", indexes = {
        @Index(name = "idx_wallet_topup_user", columnList = "user_id"),
        @Index(name = "idx_wallet_topup_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTopUpRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amountInr;

    @Column(nullable = false, length = 50)
    private String paymentMethod;

    @Column(nullable = false, unique = true, length = 150)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TopUpStatus status = TopUpStatus.PENDING;

    @Column
    private Long reviewedBy;

    @Column
    private LocalDateTime reviewedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum TopUpStatus { PENDING, APPROVED, REJECTED }
}
