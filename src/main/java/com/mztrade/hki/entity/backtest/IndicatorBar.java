package com.mztrade.hki.entity.backtest;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class IndicatorBar {
    private LocalDateTime date;
    private double value;

    public LocalDateTime getDate() {
        return date;
    }
    public IndicatorBar setDate(LocalDateTime date) {
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
