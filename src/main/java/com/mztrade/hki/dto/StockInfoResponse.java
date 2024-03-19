package com.mztrade.hki.dto;

import com.mztrade.hki.entity.StockInfo;
import com.mztrade.hki.entity.StockPrice;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder(toBuilder = true)
public class StockInfoResponse {
    private String ticker;
    private String name;
    private LocalDate listedDate;
    private Integer marketCapital;
    static public StockInfoResponse from(StockInfo stockInfo) {
        return StockInfoResponse.builder()
                .ticker(stockInfo.getTicker())
                .name(stockInfo.getName())
                .listedDate(stockInfo.getListedDate())
                .marketCapital(stockInfo.getMarketCapital())
                .build();
    }
}
