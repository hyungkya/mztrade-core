package com.mztrade.hki.entity.indicator;

import com.mztrade.hki.entity.Bar;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BollingerBandLowAlgorithm implements Algorithm {
    private final int period;
    private final int exp;
    public BollingerBandLowAlgorithm(List<Float> params) {
        this.period = params.getFirst().intValue();
        this.exp = params.get(1).intValue();
    }
    public Map<LocalDateTime, Double> calculate(List<? extends Bar> bars) {
        Map<LocalDateTime, Double> result = new HashMap<>();

        for (int i = 0; i < bars.size(); i++) {
            if (i < period) {
                result.put(bars.get(i).getDate(), Double.NaN);
            } else {
                double avg = bars.subList(i + 1 - period, i + 1)
                        .stream()
                        .mapToInt(Bar::getClose)
                        .average()
                        .orElseThrow();
                double squareSum = bars.subList(i + 1 - period, i + 1)
                        .stream()
                        .mapToDouble(b -> Math.pow(Math.abs(b.getClose() - avg), 2))
                        .sum();
                double std = Math.sqrt(squareSum / (period - 1));
                result.put(bars.get(i).getDate(), avg - (std * exp));
            }
        }

        assert result.size() == bars.size();
        return result;
    }
    public int requiredSize() {
        return period;
    }
}
