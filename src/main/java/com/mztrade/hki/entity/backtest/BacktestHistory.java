package com.mztrade.hki.entity.backtest;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(toBuilder = true)
@ToString
public class BacktestHistory {
    private int uid;
    private int aid;
    private String param;
    private double plratio;
}
