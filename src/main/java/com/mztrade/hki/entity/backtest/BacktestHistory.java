package com.mztrade.hki.entity.backtest;

import com.mztrade.hki.entity.Account;
import com.mztrade.hki.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
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
