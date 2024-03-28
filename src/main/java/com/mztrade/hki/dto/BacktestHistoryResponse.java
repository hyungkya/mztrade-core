package com.mztrade.hki.dto;

import com.mztrade.hki.entity.Order;
import com.mztrade.hki.entity.backtest.BacktestHistory;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class BacktestHistoryResponse {
    private Integer uid;
    private String uname;
    private Integer aid;
    private String param;
    private Double plratio;
    static public BacktestHistoryResponse from(BacktestHistory backtestHistory) {
        return BacktestHistoryResponse.builder()
                .uid(backtestHistory.getUser().getUid())
                .uname(backtestHistory.getUser().getName())
                .aid(backtestHistory.getAccount().getAid())
                .param(backtestHistory.getParam())
                .plratio(backtestHistory.getPlratio())
                .build();
    }
}
