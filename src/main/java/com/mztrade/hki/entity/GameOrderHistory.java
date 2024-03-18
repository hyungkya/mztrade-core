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
    @Id
    @Column(nullable = false)
    @JoinColumn(name = "game_history", referencedColumnName = "gid")
    private int gid;
    @Id
    @Column(nullable = false)
    @JoinColumn(name = "order_history", referencedColumnName = "oid")
    private int oid;
}
