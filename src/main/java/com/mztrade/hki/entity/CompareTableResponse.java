package com.mztrade.hki.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder(toBuilder = true)
@ToString
public class CompareTableResponse {
    private int aid;
    private String ticker;
    private String title;
    private String subTitle;
    private double plratio;
    private double winRate;
    private double frequency;
}




