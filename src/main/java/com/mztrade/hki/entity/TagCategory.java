package com.mztrade.hki.entity;

public enum TagCategory {
    STOCK_INFO(1),
    BACKTEST_HISTORY(2);

    private final Integer id;
    TagCategory(Integer id) {
        this.id = id;
    }

    public Integer id() {
        return this.id;
    }
}
