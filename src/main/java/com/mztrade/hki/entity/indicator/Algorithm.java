package com.mztrade.hki.entity.indicator;

import com.mztrade.hki.entity.Bar;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface Algorithm {
    Map<LocalDateTime, Double> calculate(List<? extends Bar> bars);
    int requiredSize();
}
