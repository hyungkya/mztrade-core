package com.mztrade.hki.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Builder(toBuilder = true)
@ToString
@Entity
@Table(name = "account")
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private int aid;
    @JoinColumn(nullable = false, table = "customers", referencedColumnName = "uid")
    private int uid;
    @Column(nullable = false, columnDefinition = "0")
    private long balance;
    @Column(length = 16, nullable = false, columnDefinition = "BACKTEST")
    @Builder.Default
    private String type = "BACKTEST";
}
