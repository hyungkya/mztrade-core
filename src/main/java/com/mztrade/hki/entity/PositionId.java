package com.mztrade.hki.entity;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class PositionId implements Serializable {
    @EqualsAndHashCode.Include
    public Account account;

    @EqualsAndHashCode.Include
    public StockInfo stockInfo;
}
