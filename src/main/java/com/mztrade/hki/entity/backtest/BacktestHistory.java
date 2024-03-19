package com.mztrade.hki.entity.backtest;

import com.mztrade.hki.entity.Account;
import com.mztrade.hki.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
@ToString @EqualsAndHashCode

@Table(name = "backtest_history")
@Entity
public class BacktestHistory {
    @Id
    @Column(name = "aid", nullable = false)
    private Integer aid;
    @ManyToOne @MapsId
    @JoinColumn(name = "aid", nullable = false)
    private Account account;
    @ManyToOne
    @JoinColumn(name = "uid", nullable = false)
    private User user;
    @Column
    private String param;
    @Column
    private double plratio;
}
