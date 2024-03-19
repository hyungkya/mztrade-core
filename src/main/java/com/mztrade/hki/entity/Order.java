package com.mztrade.hki.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
@ToString @EqualsAndHashCode

@Table(name = "order_history")
@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer oid;
    @ManyToOne
    @JoinColumn(name = "aid")
    private Account account;
    @Column(nullable = false)
    private LocalDateTime filledTime;
    @JoinColumn(name = "order_type", referencedColumnName = "otid")
    private Integer otid;
    @ManyToOne
    @JoinColumn(name = "ticker")
    private StockInfo stockInfo;
    @Column(nullable = false)
    private Integer qty;
    @Column(nullable = false)
    private Integer price;
    @Column
    private BigDecimal avgEntryPrice;
}
