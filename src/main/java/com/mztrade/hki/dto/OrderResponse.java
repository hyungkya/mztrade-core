package com.mztrade.hki.dto;

import com.mztrade.hki.entity.Order;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class OrderResponse {
    private Integer oid;
    private Integer aid;
    private LocalDateTime filledTime;
    private Integer otid;
    private String ticker;
    private Integer qty;
    private Integer price;
    private BigDecimal avgEntryPrice;
    static public OrderResponse from(Order order) {
        return OrderResponse.builder()
                .oid(order.getOid())
                .aid(order.getAccount().getAid())
                .filledTime(order.getFilledTime())
                .otid(order.getOtid())
                .ticker(order.getStockInfo().getTicker())
                .qty(order.getQty())
                .price(order.getPrice())
                .avgEntryPrice(order.getAvgEntryPrice())
                .build();
    }
}
