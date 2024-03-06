package com.mztrade.hki.entity;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

public class Bar {
    private String ticker;
    private LocalDateTime date;
    private Integer open;
    private Integer high;
    private Integer low;
    private Integer close;
    private Long volume;

    public String getTicker() {
        return ticker;
    }

    public Bar setTicker(String ticker) {
        this.ticker = ticker;
        return this;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public Bar setDate(LocalDateTime date) {
        this.date = date;
        return this;
    }

    public Integer getOpen() {
        return open;
    }

    public Bar setOpen(Integer open) {
        this.open = open;
        return this;
    }

    public Integer getHigh() {
        return high;
    }

    public Bar setHigh(Integer high) {
        this.high = high;
        return this;
    }

    public Integer getLow() {
        return low;
    }

    public Bar setLow(Integer low) {
        this.low = low;
        return this;
    }

    public Integer getClose() {
        return close;
    }

    public Bar setClose(Integer close) {
        this.close = close;
        return this;
    }

    public Long getVolume() {
        return volume;
    }

    public Bar setVolume(Long volume) {
        this.volume = volume;
        return this;
    }

    @Override
    public String toString() {
        return "Bar{" +
                "ticker='" + ticker + '\'' +
                ", date=" + date +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", volume=" + volume +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Bar bar = (Bar) o;
        return Objects.equals(ticker, bar.ticker) && Objects.equals(date, bar.date)
                && Objects.equals(open, bar.open) && Objects.equals(high, bar.high)
                && Objects.equals(low, bar.low) && Objects.equals(close, bar.close)
                && Objects.equals(volume, bar.volume);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticker, date, open, high, low, close, volume);
    }

    public static Comparator<Bar> COMPARE_BY_DATE = new Comparator<Bar>() {
        @Override
        public int compare(Bar o1, Bar o2) {
            return o1.getDate().compareTo(o2.getDate());
        }
    };
}
