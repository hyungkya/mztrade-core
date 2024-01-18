package com.mztrade.hki.entity;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StockInfo {
    private String ticker;
    private String name;
    private LocalDate listedDate;
    private Integer marketCapital;
}
