package com.mztrade.hki.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
