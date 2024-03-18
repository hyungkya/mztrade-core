package com.mztrade.hki.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder(toBuilder = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor

@Table(name = "order_history")
@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer oid;
    @JoinColumn(name = "account", referencedColumnName = "aid")
    private Integer aid;
    @Column(nullable = false)
    private LocalDateTime filledTime;
    @JoinColumn(name = "order_type", referencedColumnName = "otid")
    private Integer otid;
    @JoinColumn(name = "stock_info", referencedColumnName = "ticker")
    private String ticker;
    @Column(nullable = false)
    private Integer qty;
    @Column(nullable = false)
    private Integer price;
    @Column
    private BigDecimal avgEntryPrice;
}
