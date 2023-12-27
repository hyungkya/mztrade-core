package com.mztrade.hki.entity;

import java.math.BigDecimal;
import java.util.Objects;

public class Position {
    private Integer aid;
    private String ticker;
    private Integer qty;
    private BigDecimal avgEntryPrice;

    public Integer getAid() {
        return aid;
    }

    public Position setAid(Integer aid) {
        this.aid = aid;
        return this;
    }

    public String getTicker() {
        return ticker;
    }

    public Position setTicker(String ticker) {
        this.ticker = ticker;
        return this;
    }

    public Integer getQty() {
        return qty;
    }

    public Position setQty(Integer qty) {
        this.qty = qty;
        return this;
    }

    public BigDecimal getAvgEntryPrice() {
        return avgEntryPrice;
    }

    public Position setAvgEntryPrice(BigDecimal avgEntryPrice) {
        this.avgEntryPrice = avgEntryPrice;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Position position = (Position) o;
        return Objects.equals(aid, position.aid) && Objects.equals(ticker,
                position.ticker) && Objects.equals(qty, position.qty)
                && avgEntryPrice.compareTo(position.avgEntryPrice) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(aid, ticker, qty, avgEntryPrice);
    }
}
