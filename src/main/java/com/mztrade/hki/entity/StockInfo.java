package com.mztrade.hki.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
    @Column(name = "listed_market")
    private String listedMarket;
    @Column(name = "industry")
    private String industry;
    @Column(name = "capital")
    private Integer capital;
    @Column(name = "market_capital")
    private Integer marketCapital;
    @Column(name = "par_value")
    private Integer parValue;
    @Column(name = "issued_shares")
    private Long issuedShares;
    @Column(name = "per")
    private Double per;
    @Column(name = "eps")
    private Integer eps;
    @Column(name = "pbr")
    private Double pbr;
    //@OneToMany(mappedBy = "stockInfo")
    //private Set<StockPrice> stockPrices = new HashSet<>();
    //@OneToMany(mappedBy = "stockInfo")
    //private Set<Position> positions = new HashSet<>();
    //@OneToMany(mappedBy = "stockInfo")
    //private Set<Order> orders = new HashSet<>();
}
