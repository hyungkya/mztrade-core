package com.mztrade.hki.entity.indicator;

import com.mztrade.hki.entity.Bar;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovingAverageConvergenceDivergenceSignalAlgorithm implements Algorithm {
    private final int shortPeriod;
    private final int longPeriod;
    private final int signalPeriod;

    public MovingAverageConvergenceDivergenceSignalAlgorithm(List<Float> params) {
        this.shortPeriod = params.getFirst().intValue();
        this.longPeriod = params.get(1).intValue();
        this.signalPeriod = params.get(2).intValue();
    }

    public Map<LocalDateTime, Double> calculate(List<? extends Bar> bars) {
        Map<LocalDateTime, Double> result = new HashMap<>();

        List<Double> shortEMA = new ArrayList<>();
        double previousEMA = Double.NaN;
        double smoothingConstant = 1 - (2.0 / (shortPeriod + 1));
        for (int i = 0; i < bars.size(); i++) {
            if (i < shortPeriod) {
                shortEMA.add(Double.NaN);
            } else if (i == shortPeriod) {
                previousEMA = bars.subList(i - shortPeriod, i).stream().mapToInt(b -> b.getClose()).average().getAsDouble();
                shortEMA.add(previousEMA);
            } else {
                previousEMA = (smoothingConstant * (bars.get(i).getClose() - previousEMA)) + previousEMA;
                shortEMA.add(previousEMA);
            }
        }

        List<Double> longEMA = new ArrayList<>();
        previousEMA = Double.NaN;
        smoothingConstant = 1 - (2.0 / (longPeriod + 1));
        for (int i = 0; i < bars.size(); i++) {
            if (i < longPeriod) {
                longEMA.add(Double.NaN);
            } else if (i == longPeriod) {
                previousEMA = bars.subList(i - longPeriod, i).stream().mapToInt(b -> b.getClose()).average().getAsDouble();
                longEMA.add(previousEMA);
            } else {
                previousEMA = (smoothingConstant * (bars.get(i).getClose() - previousEMA)) + previousEMA;
                longEMA.add(previousEMA);
            }
        }

        List<Double> MACDs = new ArrayList<>();
        for (int i = 0; i < bars.size(); i++) {
            if (i < longPeriod) {
                MACDs.add(Double.NaN);
            } else {
                MACDs.add(longEMA.get(i) - shortEMA.get(i));
            }
        }

        double k = 2 / (signalPeriod + 1);
        for (int i = 1; i < bars.size(); i++) {
            result.put(bars.get(i).getDate(), ((1 - k) * MACDs.get(i - 1)) + (k * MACDs.get(i)));
        }

        assert result.size() == bars.size();
        return result;
    }

    public int requiredSize() {
        return longPeriod + signalPeriod;
    }
}
