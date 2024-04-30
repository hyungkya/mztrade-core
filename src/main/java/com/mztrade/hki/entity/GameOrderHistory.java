package com.mztrade.hki.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
