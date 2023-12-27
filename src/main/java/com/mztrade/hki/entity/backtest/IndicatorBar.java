package com.mztrade.hki.entity.backtest;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class IndicatorBar {
    private Instant date;
    private double value;

    public Instant getDate() {
        return date;
    }
    public IndicatorBar setDate(Instant date) {
        this.date = date;
        return this;
    }

    public double getValue() {
        return value;
    }

    public IndicatorBar setValue(double value) {
        this.value = value;
        return this;
    }
}
