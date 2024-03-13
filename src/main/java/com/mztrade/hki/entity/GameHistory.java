package com.mztrade.hki.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
@ToString
public class GameHistory {
    private int aid;
    private int gid;
    private String ticker;
    private LocalDateTime startDate;
    private int turns;
    private double plratio;
}
