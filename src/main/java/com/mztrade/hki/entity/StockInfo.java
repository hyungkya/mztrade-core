package com.mztrade.hki.entity;

import java.time.LocalDate;

public class StockInfo {
    private String ticker;
    private String name;
    private LocalDate listedDate;
    private Integer marketCapital;

    public String getTicker() {
        return ticker;
    }

    public StockInfo setTicker(String ticker) {
        this.ticker = ticker;
        return this;
    }

    public String getName() {
        return name;
    }

    public StockInfo setName(String name) {
        this.name = name;
        return this;
    }

    public LocalDate getListedDate() {
        return listedDate;
    }

    public StockInfo setListedDate(LocalDate listedDate) {
        this.listedDate = listedDate;
        return this;
    }

    public Integer getMarketCapital() {
        return marketCapital;
    }

    public StockInfo setMarketCapital(Integer marketCapital) {
        this.marketCapital = marketCapital;
        return this;
    }
}
