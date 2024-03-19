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
@Table(name = "game_order_history")
@IdClass(GameOrderHistoryId.class)
@Entity
public class GameOrderHistory {
    @Id @ManyToOne
    @JoinColumn(name = "gid")
    private GameHistory gameHistory;
    @Id @OneToOne
    @JoinColumn(name = "oid")
    private Order order;
}
