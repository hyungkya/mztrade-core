package com.mztrade.hki.entity;

import java.math.BigDecimal;
import java.time.Instant;

public class Order {
    private Integer oid;
    private Integer aid;
    private Instant filledTime;
    private Integer otid;
    private String ticker;
    private Integer qty;
    private Integer price;
    private BigDecimal avgEntryPrice;

    public Integer getOid() {
        return oid;
    }

    public Order setOid(Integer oid) {
        this.oid = oid;
        return this;
    }

    public Integer getAid() {
        return aid;
    }

    public Order setAid(Integer aid) {
        this.aid = aid;
        return this;
    }

    public Instant getFilledTime() {
        return filledTime;
    }

    public Order setFilledTime(Instant filledTime) {
        this.filledTime = filledTime;
        return this;
    }

    public Integer getOtid() {
        return otid;
    }

    public Order setOtid(Integer otid) {
        this.otid = otid;
        return this;
    }

    public String getTicker() {
        return ticker;
    }

    public Order setTicker(String ticker) {
        this.ticker = ticker;
        return this;
    }

    public Integer getQty() {
        return qty;
    }

    public Order setQty(Integer qty) {
        this.qty = qty;
        return this;
    }

    public Integer getPrice() {
        return price;
    }

    public Order setPrice(Integer price) {
        this.price = price;
        return this;
    }

    public BigDecimal getAvgEntryPrice() {
        return avgEntryPrice;
    }

    public Order setAvgEntryPrice(BigDecimal avgEntryPrice) {
        this.avgEntryPrice = avgEntryPrice;
        return this;
    }
}
