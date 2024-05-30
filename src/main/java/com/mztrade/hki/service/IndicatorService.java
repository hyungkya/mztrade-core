package com.mztrade.hki.service;

import com.mztrade.hki.entity.Bar;
import com.mztrade.hki.entity.indicator.Indicator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IndicatorService {
    public int requiredBars(String type, List<Float> params) {
        Indicator indicator = new Indicator()
                .setAlgorithm(type, params);
        return indicator.requiredSize();
    }
    public double getIndicator(List<? extends Bar> bars, LocalDateTime date, String type, List<Float> params) {
        Indicator indicator = new Indicator()
                .setAlgorithm(type, params);
        indicator.setBars(bars);
        //TODO::예외처리
        return indicator.calculate().get(date);
    }

    public Map<LocalDateTime, Double> getIndicators(List<? extends Bar> bars, String type, List<Float> params) {
        Indicator indicator = new Indicator()
                .setAlgorithm(type, params)
                .setBars(bars);
        return indicator.calculate();
    }
}
