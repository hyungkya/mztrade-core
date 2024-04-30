package com.mztrade.hki.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class StockPriceId implements Serializable {
    @EqualsAndHashCode.Include
    public StockInfo stockInfo;

    @EqualsAndHashCode.Include
    public LocalDateTime date;
}
