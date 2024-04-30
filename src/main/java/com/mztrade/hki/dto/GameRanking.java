package com.mztrade.hki.dto;

import com.mztrade.hki.entity.GameHistory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class GameRanking {
    private String name;
    private long finalBalance;
    static public GameRanking from(GameHistory gameHistory) {
        return GameRanking.builder()
                .name(gameHistory.getAccount().getUser().getName())
                .finalBalance(gameHistory.getFinalBalance())
                .build();
    }
}
