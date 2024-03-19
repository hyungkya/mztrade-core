package com.mztrade.hki.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
@ToString @EqualsAndHashCode

@Table(name = "stock_info")
@Entity
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
    //@OneToMany(mappedBy = "stockInfo")
    //private Set<StockPrice> stockPrices = new HashSet<>();
    //@OneToMany(mappedBy = "stockInfo")
    //private Set<Position> positions = new HashSet<>();
    //@OneToMany(mappedBy = "stockInfo")
    //private Set<Order> orders = new HashSet<>();
}
