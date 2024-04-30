package com.mztrade.hki.dto;

import com.mztrade.hki.entity.StockInfo;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class StockInfoResponse {
    private String ticker;
    private String name;
    private LocalDate listedDate;
    private String listedMarket;
    private String industry;
    private Integer marketCapital;
    static public StockInfoResponse from(StockInfo stockInfo) {
        return StockInfoResponse.builder()
                .ticker(stockInfo.getTicker())
                .name(stockInfo.getName())
                .listedDate(stockInfo.getListedDate())
                .listedMarket(stockInfo.getListedMarket())
                .industry(stockInfo.getIndustry())
                .marketCapital(stockInfo.getMarketCapital())
                .build();
    }
}
