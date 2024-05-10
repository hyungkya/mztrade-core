package com.mztrade.hki.entity;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class BacktestResultTagId implements Serializable {
    @EqualsAndHashCode.Include
    public Tag tag;

    @EqualsAndHashCode.Include
    public Account account;
}
