package com.mztrade.hki.entity.backtest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import lombok.*;

@Getter
@Builder(toBuilder = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor

@Entity
public class BacktestHistory {
    @JoinColumn(name = "customers", nullable = false)
    private int uid;
    @Id
    @JoinColumn(name = "account", nullable = false)
    private int aid;
    @Column
    private String param;
    @Column
    private double plratio;
}
