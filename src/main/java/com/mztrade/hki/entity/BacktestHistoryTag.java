package com.mztrade.hki.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@Entity
@NoArgsConstructor
@IdClass(BacktestHistoryTagId.class)
@Table(name = "backtest_history_tag")
@ToString
@AllArgsConstructor
public class BacktestHistoryTag {
    @Id @ManyToOne
    @JoinColumn(name = "tid")
    private Tag tag;
    @Id @ManyToOne
    @JoinColumn(name = "aid")
    private Account account;
}
