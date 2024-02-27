package com.mztrade.hki.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder(toBuilder = true)
@ToString
public class ChartSetting {
    private int uid;
    private String indicator;
}
