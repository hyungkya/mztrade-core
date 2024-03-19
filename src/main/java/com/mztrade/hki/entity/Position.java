package com.mztrade.hki.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Objects;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
@ToString @EqualsAndHashCode

@IdClass(PositionId.class)
@Table(name = "position")
@Entity
public class Position {
    @Id
    @ManyToOne
    @JoinColumn(name = "aid")
    private Account account;
    @Id
    @ManyToOne
    @JoinColumn(name = "ticker")
    private StockInfo stockInfo;
    @Column(nullable = false)
    private Integer qty;
    @Column(nullable = false)
    private BigDecimal avgEntryPrice;
}
