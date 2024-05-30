package com.mztrade.hki.entity.indicator;

import com.mztrade.hki.entity.Bar;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PivotPointResist2Algorithm implements Algorithm {
    private final int period;

    public PivotPointResist2Algorithm(List<Float> params) {
        this.period = params.getFirst().intValue();
    }

    public Map<LocalDateTime, Double> calculate(List<? extends Bar> bars) {
        Map<LocalDateTime, Double> result = new HashMap<>();

        int currentDuration = 0;
        double r2 = Double.NaN;
        for (int i = 0; i < bars.size(); i += period) {
            int high = 0;
            int low = 0;
            int close = bars.subList(i, i + period <= bars.size() ? i + period : bars.size()).getLast().getClose();
            for (int j = i; j < i + period && j < bars.size(); j++) {
                high = bars.get(j).getHigh() > high ? bars.get(j).getHigh() : high;
                low = low == 0 || bars.get(j).getLow() < low ? bars.get(j).getLow() : low;
                result.put(bars.get(j).getDate(), r2);
            }
            double pp = (high + low + close) / 3;
            r2 = pp + (high - low);
        }

        assert result.size() == bars.size();
        return result;
    }

    public int requiredSize() {
        return period;
    }
}
