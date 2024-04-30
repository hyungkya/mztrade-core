package com.mztrade.hki.dto;

import com.mztrade.hki.entity.Position;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class PositionResponse {
    private Integer aid;
    private String ticker;
    private Integer qty;
    private BigDecimal avgEntryPrice;
    static public PositionResponse from(Position position) {
        return PositionResponse.builder()
                .aid(position.getAccount().getAid())
                .ticker(position.getStockInfo().getTicker())
                .qty(position.getQty())
                .avgEntryPrice(position.getAvgEntryPrice())
                .build();
    }
}
