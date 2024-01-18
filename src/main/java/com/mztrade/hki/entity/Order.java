package com.mztrade.hki.entity;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Order {
    private Integer oid;
    private Integer aid;
    private Instant filledTime;
    private Integer otid;
    private String ticker;
    private Integer qty;
    private Integer price;
    private BigDecimal avgEntryPrice;
}
