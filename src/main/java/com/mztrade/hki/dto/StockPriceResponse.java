package com.mztrade.hki.dto;

import com.mztrade.hki.entity.StockPrice;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

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
    static public StockPriceResponse from(StockPrice stockPrice) {
        return StockPriceResponse.builder()
                .ticker(stockPrice.getStockInfo().getTicker())
                .date(stockPrice.getDate())
                .open(stockPrice.getOpen())
                .high(stockPrice.getHigh())
                .low(stockPrice.getLow())
                .close(stockPrice.getClose())
                .volume(stockPrice.getVolume())
                .build();
    }
}
