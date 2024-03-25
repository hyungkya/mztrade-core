package com.mztrade.hki.dto;

import com.mztrade.hki.Util;
import com.mztrade.hki.entity.backtest.Condition;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter @ToString
public class BacktestRequest {
    private Integer uid;
    private String title;
    private List<String> tickers;
    private String startDate;
    private String endDate;
    private Long initialBalance;
    private List<Condition> buyConditions;
    private Integer buyConditionLimit;
    private List<Condition> sellConditions;
    private Integer sellConditionLimit;
    private List<Double> dca;
    private Double stopLoss;
    private Double stopProfit;
    private Double trailingStop;

    public LocalDateTime parseStartDate() {
        return LocalDateTime.parse(Util.formatDate(this.startDate));
    }

    public LocalDateTime parseEndDate() {
        return LocalDateTime.parse(Util.formatDate(this.endDate));
    }
}
