package com.mztrade.hki.entity.backtest;

public class BacktestHistory {
    private int uid;
    private int aid;
    private String param;

    public int getUid() {
        return uid;
    }

    public BacktestHistory setUid(int uid) {
        this.uid = uid;
        return this;
    }

    public int getAid() {
        return aid;
    }

    public BacktestHistory setAid(int aid) {
        this.aid = aid;
        return this;
    }

    public String getParam() {
        return param;
    }

    public BacktestHistory setParam(String param) {
        this.param = param;
        return this;
    }
}
