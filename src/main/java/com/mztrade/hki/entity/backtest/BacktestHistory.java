package com.mztrade.hki.entity.backtest;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class BacktestHistory {
    private int uid;
    private int aid;
    private String param;
    private double plratio;
}
