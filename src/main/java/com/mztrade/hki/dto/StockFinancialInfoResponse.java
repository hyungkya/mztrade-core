package com.mztrade.hki.dto;

import com.mztrade.hki.entity.StockInfo;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class StockFinancialInfoResponse {
    private String ticker;
    private String name;
    private LocalDate listedDate;
    private Integer capital;
    private Integer marketCapital;
    private Integer parValue;
    private Long issuedShares;
    private Double per;
    private Integer eps;
    private Double pbr;
    static public StockFinancialInfoResponse from(StockInfo stockInfo) {
        return StockFinancialInfoResponse.builder()
                .ticker(stockInfo.getTicker())
                .name(stockInfo.getName())
                .listedDate(stockInfo.getListedDate())
                .capital(stockInfo.getCapital())
                .marketCapital(stockInfo.getMarketCapital())
                .parValue(stockInfo.getParValue())
                .issuedShares(stockInfo.getIssuedShares())
                .per(stockInfo.getPer())
                .eps(stockInfo.getEps())
                .pbr(stockInfo.getPbr())
                .build();
    }
}
