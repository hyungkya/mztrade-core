package com.mztrade.hki.dto;

import com.mztrade.hki.entity.backtest.BacktestResult;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class BacktestResultResponse {
    private Integer uid;
    private String uname;
    private Integer aid;
    private String param;
    private Double plratio;
    static public BacktestResultResponse from(BacktestResult backtestResult) {
        return BacktestResultResponse.builder()
                .uid(backtestResult.getUser().getUid())
                .uname(backtestResult.getUser().getName())
                .aid(backtestResult.getAccount().getAid())
                .param(backtestResult.getParam())
                .plratio(backtestResult.getPlratio())
                .build();
    }
}
