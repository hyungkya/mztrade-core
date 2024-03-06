package com.mztrade.hki.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class Order {
    private Integer oid;
    private Integer aid;
    private LocalDateTime filledTime;
    private Integer otid;
    private String ticker;
    private Integer qty;
    private Integer price;
    private BigDecimal avgEntryPrice;
}
