package com.mztrade.hki.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Builder
@Entity
@Table(name = "stock_info")
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StockInfo {
    @Id
    @Column(name = "ticker", unique = true, length = 16)
    private String ticker;
    @Column(name = "name", unique = true, length = 64)
    private String name;
    @Column(name = "listed_date")
    private LocalDate listedDate;
    @Column(name = "market_capital")
    private Integer marketCapital;
}
