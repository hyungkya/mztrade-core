package com.mztrade.hki.entity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class GameOrderHistoryId implements Serializable {
    @EqualsAndHashCode.Include
    public GameHistory gameHistory;

    @EqualsAndHashCode.Include
    public Order order;
}
