package com.mztrade.hki.entity;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class GameOrderHistoryId implements Serializable {
    @EqualsAndHashCode.Include
    public GameHistory gameHistory;

    @EqualsAndHashCode.Include
    public Order order;
}
