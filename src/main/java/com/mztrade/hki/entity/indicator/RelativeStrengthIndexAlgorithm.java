package com.mztrade.hki.entity.indicator;

import com.mztrade.hki.entity.Bar;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelativeStrengthIndexAlgorithm implements Algorithm {
    private final int period;

    public RelativeStrengthIndexAlgorithm(List<Float> params) {
        this.period = params.getFirst().intValue();
    }

    public Map<LocalDateTime, Double> calculate(List<? extends Bar> bars) {
        Map<LocalDateTime, Double> result = new HashMap<>();

        List<Integer> diffs = new ArrayList<>();
        for (int i = 1; i < bars.size(); i++) {
            diffs.add(bars.get(i).getClose() - bars.get(i - 1).getClose());
        }
        for (int i = 0; i < bars.size(); i++) {
            if (i < period) {
                result.put(bars.get(i).getDate(), Double.NaN);
            } else {
                double au = diffs.subList(i - period, i).stream().filter(e -> e > 0).mapToInt(Math::abs).average().orElse(0);
                double ad = diffs.subList(i - period, i).stream().filter(e -> e < 0).mapToInt(Math::abs).average().orElse(100);
                result.put(bars.get(i).getDate(), (au / (ad + au)) * 100);
            }
        }

        assert result.size() == bars.size();
        return result;
    }

    public int requiredSize() {
        return period + 1;
    }
}
