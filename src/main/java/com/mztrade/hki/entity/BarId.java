package com.mztrade.hki.entity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class BarId implements Serializable {
    @EqualsAndHashCode.Include
    public String ticker;

    @EqualsAndHashCode.Include
    public LocalDateTime date;
}
