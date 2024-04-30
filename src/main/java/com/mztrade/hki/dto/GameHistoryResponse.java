package com.mztrade.hki.dto;

import com.mztrade.hki.entity.GameHistory;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class GameHistoryResponse {
    private int aid;
    private int gid;
    private String ticker;
    private LocalDateTime startDate;
    private int turns;
    private int maxTurn;
    private long startBalance;
    private long finalBalance;
    private boolean finished;
    static public GameHistoryResponse from(GameHistory gameHistory) {
        return GameHistoryResponse.builder()
                .aid(gameHistory.getAccount().getAid())
                .gid(gameHistory.getGid())
                .ticker(gameHistory.getStockInfo().getTicker())
                .startDate(gameHistory.getStartDate())
                .turns(gameHistory.getTurns())
                .maxTurn(gameHistory.getMaxTurn())
                .startBalance(gameHistory.getStartBalance())
                .finalBalance(gameHistory.getFinalBalance())
                .finished(gameHistory.isFinished())
                .build();
    }
}
