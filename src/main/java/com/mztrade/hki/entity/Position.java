package com.mztrade.hki.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@IdClass(PositionId.class)
@Table(name = "position")
@Entity
public class Position {
    @Id
    @JoinColumn(name = "account", referencedColumnName = "aid")
    private Integer aid;
    @Id
    @JoinColumn(name = "stock_info", referencedColumnName = "ticker")
    private String ticker;
    @Column(nullable = false)
    private Integer qty;
    @Column(nullable = false)
    private BigDecimal avgEntryPrice;
}
