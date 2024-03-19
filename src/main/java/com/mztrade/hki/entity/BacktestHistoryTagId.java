package com.mztrade.hki.entity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class BacktestHistoryTagId implements Serializable {
    @EqualsAndHashCode.Include
    public Tag tag;

    @EqualsAndHashCode.Include
    public Account account;
}
