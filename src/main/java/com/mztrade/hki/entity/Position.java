package com.mztrade.hki.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
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
