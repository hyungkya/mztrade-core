package com.mztrade.hki.entity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class StockPriceId implements Serializable {
    @EqualsAndHashCode.Include
    public StockInfo stockInfo;

    @EqualsAndHashCode.Include
    public LocalDateTime date;
}
