package com.mztrade.hki.entity.backtest;

import com.mztrade.hki.Util;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class BacktestRequest {
    private String uid;

    private String title;
    private String initialBalance;
    private List<List<ConditionRequest>> buyConditions;
    private List<List<ConditionRequest>> sellConditions;
    private List<Float> dca;
    private String maxTrading;
    private List<String> tickers;
    private String startDate;
    private String endDate;

    public String getUid() {
        return uid;
    }

    public BacktestRequest setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public BacktestRequest setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getInitialBalance() {
        return initialBalance;
    }

    public BacktestRequest setInitialBalance(String initialBalance) {
        this.initialBalance = initialBalance;
        return this;
    }

    public List<List<ConditionRequest>> getBuyConditions() {
        return buyConditions;
    }

    public BacktestRequest setBuyConditions(List<List<ConditionRequest>> buyConditions) {
        this.buyConditions = buyConditions;
        return this;
    }

    public List<List<ConditionRequest>> getSellConditions() {
        return sellConditions;
    }

    public BacktestRequest setSellConditions(List<List<ConditionRequest>> sellConditions) {
        this.sellConditions = sellConditions;
        return this;
    }

    public List<Float> getDca() {
        return dca;
    }

    public BacktestRequest setDca(List<Float> dca) {
        this.dca = dca;
        return this;
    }

    public String getMaxTrading() {
        return maxTrading;
    }

    public BacktestRequest setMaxTrading(String maxTrading) {
        this.maxTrading = maxTrading;
        return this;
    }

    public List<String> getTickers() {
        return tickers;
    }

    public BacktestRequest setTickers(List<String> tickers) {
        this.tickers = tickers;
        return this;
    }

    public String getStartDate() {
        return startDate;
    }

    public BacktestRequest setStartDate(String startDate) {
        this.startDate = startDate;
        return this;
    }

    public String getEndDate() {
        return endDate;
    }

    public BacktestRequest setEndDate(String endDate) {
        this.endDate = endDate;
        return this;
    }

    public int parseUid() {
        return Integer.parseInt(this.uid);
    }

    public long parseInitialBalance() {
        return Long.parseLong(this.initialBalance);
    }

    public List<List<Condition>> parseBuyConditions() {
        List<List<Condition>> buyConditions = new ArrayList<>();
        for (List<ConditionRequest> buyConditionRequests : this.buyConditions) {
            List<Condition> buyConditionGroup = new ArrayList<>();
            for (ConditionRequest buyConditionRequest : buyConditionRequests) {
                Condition buyCondition = new Condition()
                        .setBaseIndicator(buyConditionRequest.parseBaseIndicator())
                        .setTargetIndicator(buyConditionRequest.parseTargetIndicator())
                        .setConstantBound(buyConditionRequest.parseConstantBound())
                        .setCompareType(buyConditionRequest.getCompareType())
                        .setFrequency(buyConditionRequest.parseFrequency());
                buyConditionGroup.add(buyCondition);
            }
            buyConditions.add(buyConditionGroup);
        }
        return buyConditions;
    }

    public List<List<Condition>> parseSellConditions() {
        List<List<Condition>> sellConditions = new ArrayList<>();
        for (List<ConditionRequest> sellConditionRequests : this.sellConditions) {
            List<Condition> sellConditionGroup = new ArrayList<>();
            for (ConditionRequest sellConditionRequest : sellConditionRequests) {
                Condition sellCondition = new Condition()
                        .setBaseIndicator(sellConditionRequest.parseBaseIndicator())
                        .setTargetIndicator(sellConditionRequest.parseTargetIndicator())
                        .setConstantBound(sellConditionRequest.parseConstantBound())
                        .setCompareType(sellConditionRequest.getCompareType())
                        .setFrequency(sellConditionRequest.parseFrequency());
                sellConditionGroup.add(sellCondition);
            }
            sellConditions.add(sellConditionGroup);
        }
        return sellConditions;
    }

    public Integer parseMaxTrading() {
        return Integer.parseInt(this.maxTrading);
    }

    public Instant parseStartDate() {
        return Instant.parse(Util.formatDate(this.startDate));
    }

    public Instant parseEndDate() {
        return Instant.parse(Util.formatDate(this.endDate));
    }

}
