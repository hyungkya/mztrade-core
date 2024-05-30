package com.mztrade.hki.entity.indicator;

import com.mztrade.hki.entity.Bar;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExponentialMovingAverageAlgorithm implements Algorithm {
    private final int period;

    public ExponentialMovingAverageAlgorithm(List<Float> params) {
        this.period = params.getFirst().intValue();
    }

    public Map<LocalDateTime, Double> calculate(List<? extends Bar> bars) {
        Map<LocalDateTime, Double> result = new HashMap<>();

        double previousEMA = Double.NaN;
        double smoothingConstant = 1 - (2.0 / (period + 1));
        for (int i = 0; i < bars.size(); i++) {
            if (i < period) {
                result.put(bars.get(i).getDate(), Double.NaN);
            } else if (i == period) {
                previousEMA = bars.subList(i - period, i).stream().mapToInt(b -> b.getClose()).average().getAsDouble();
                result.put(bars.get(i).getDate(), previousEMA);
            } else {
                previousEMA = (smoothingConstant * (bars.get(i).getClose() - previousEMA)) + previousEMA;
                result.put(bars.get(i).getDate(), previousEMA);
            }
        }

        assert result.size() == bars.size();
        return result;
    }

    public int requiredSize() {
        return period;
    }
}
