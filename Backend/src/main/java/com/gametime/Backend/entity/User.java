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
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Tier tier = Tier.BRONZE;

    @Column(nullable = false)
    @Builder.Default
    private Integer level = 1;

    @Column(nullable = false)
    @Builder.Default
    private Integer ratingPoints = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer streak = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer globalRank = 0;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal usdtBalance = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2, columnDefinition = "numeric(15,2) default 0.00")
    @Builder.Default
    private BigDecimal inrBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Long nexusTokens = 0L;

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal pendingBalance = BigDecimal.ZERO;

    @Column(length = 10)
    private String country;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_tournaments",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "tournament_id")
    )
    @Builder.Default
    private Set<Tournament> joinedTournaments = new HashSet<>();

    public enum UserStatus { ACTIVE, SUSPENDED, BANNED }
    public enum UserRole   { USER, ADMIN }
    public enum Tier       { BRONZE, SILVER, GOLD, PLATINUM, DIAMOND, MYTHIC }
}
