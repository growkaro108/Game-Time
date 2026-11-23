package com.gametime.Backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tournament_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Column(nullable = false, length = 1000)
    private String text;

    @ElementCollection
    @CollectionTable(name = "tournament_question_options",
            joinColumns = @JoinColumn(name = "question_id"))
    @OrderColumn(name = "option_order")
    @Column(name = "option_text", nullable = false, length = 500)
    @Builder.Default
    private List<String> options = new ArrayList<>();

    @Column(nullable = false)
    private Integer correctAnswerIndex;

    @Column(nullable = false)
    @Builder.Default
    private Integer timeLimitSeconds = 10;
}
