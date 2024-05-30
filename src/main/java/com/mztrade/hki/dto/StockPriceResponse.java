package com.mztrade.hki.dto;

import com.mztrade.hki.entity.DailyStockPrice;
import java.time.LocalDateTime;

import com.mztrade.hki.entity.MinutelyStockPrice;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class StockPriceResponse {
    private String ticker;
    private LocalDateTime date;
    private Integer open;
    private Integer high;
    private Integer low;
    private Integer close;
    private Long volume;
    static public StockPriceResponse from(DailyStockPrice dailyStockPrice) {
        return StockPriceResponse.builder()
                .ticker(dailyStockPrice.getStockInfo().getTicker())
                .date(dailyStockPrice.getDate())
                .open(dailyStockPrice.getOpen())
                .high(dailyStockPrice.getHigh())
                .low(dailyStockPrice.getLow())
                .close(dailyStockPrice.getClose())
                .volume(dailyStockPrice.getVolume())
                .build();
    }

    static public StockPriceResponse from(MinutelyStockPrice minutelyStockPrice) {
        return StockPriceResponse.builder()
                .ticker(minutelyStockPrice.getStockInfo().getTicker())
                .date(minutelyStockPrice.getDate())
                .open(minutelyStockPrice.getOpen())
                .high(minutelyStockPrice.getHigh())
                .low(minutelyStockPrice.getLow())
                .close(minutelyStockPrice.getClose())
                .volume(minutelyStockPrice.getVolume())
                .build();
    }
}
