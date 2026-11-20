package com.gametime.Backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tournaments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 100)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TournamentStatus status = TournamentStatus.SCHEDULED;

    @Column(nullable = false)
    @Builder.Default
    private Integer currentPlayers = 0;

    @Column(nullable = false)
    private Integer maxPlayers;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal prizePool;

    @Column(nullable = false)
    @Builder.Default
    private Long entryFeeNexus = 0L;

    @Column(nullable = false)
    @Builder.Default
    private Integer questionCount = 10;

    @Column(nullable = false)
    private LocalDateTime startsAt;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal revenue = BigDecimal.ZERO;

    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToMany(mappedBy = "joinedTournaments", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<User> registeredUsers = new HashSet<>();

    public enum TournamentStatus { SCHEDULED, LIVE, COMPLETED, CANCELLED }
}
