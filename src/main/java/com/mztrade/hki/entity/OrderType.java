package com.mztrade.hki.entity;

public enum OrderType {
    BUY(1),
    SELL(2);

    private final Integer id;
    OrderType(Integer id) {
        this.id = id;
    }

    public Integer id() {
        return this.id;
    }
}
