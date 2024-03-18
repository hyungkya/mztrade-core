package com.mztrade.hki.entity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class PositionId implements Serializable {
    @EqualsAndHashCode.Include
    public int aid;

    @EqualsAndHashCode.Include
    public String ticker;
}
