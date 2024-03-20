package com.mztrade.hki.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Table(name = "game_history")
@Entity
public class GameHistory {
    @ManyToOne
    @JoinColumn(name = "aid")
    private Account account;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private int gid;
    @ManyToOne
    @JoinColumn(name = "ticker")
    private StockInfo stockInfo;
    @Column(nullable = false)
    private LocalDateTime startDate;
    @Column(nullable = false,columnDefinition = "0")
    @Builder.Default
    private int turns = 0;
    @Column(nullable = false,columnDefinition = "100")
    @Builder.Default
    private int maxTurn = 100;
    @Column(nullable = false)
    private long startBalance;
    @Column(nullable = false,columnDefinition = "0")
    @Builder.Default
    private long finalBalance = 0;
    @Column(nullable = false,columnDefinition = "false")
    @Builder.Default
    private boolean finished = false;
}
