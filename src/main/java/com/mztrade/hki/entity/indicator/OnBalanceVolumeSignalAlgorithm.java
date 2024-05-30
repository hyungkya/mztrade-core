package com.mztrade.hki.entity.indicator;

import com.mztrade.hki.entity.Bar;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnBalanceVolumeSignalAlgorithm implements Algorithm {
    private final int period;

    public OnBalanceVolumeSignalAlgorithm(List<Float> params) {
        this.period = params.getFirst().intValue();

    }

    public Map<LocalDateTime, Double> calculate(List<? extends Bar> bars) {
        Map<LocalDateTime, Double> result = new HashMap<>();

        double OBV = 0;
        List<Double> OBVs = new ArrayList<>();
        for (int i = 0; i < bars.size(); i++) {
            if (i == 0) {
                OBV = bars.get(i).getVolume();
                OBVs.add(OBV);
            } else {
                if (bars.get(i).getClose() > bars.get(i - 1).getClose()) {
                    OBV += bars.get(i).getVolume();
                } else if (bars.get(i).getClose() < bars.get(i - 1).getClose()) {
                    OBV -= bars.get(i).getVolume();
                }
                OBVs.add(OBV);
            }
        }

        for (int i = 0; i < bars.size(); i++) {
            if (i < period) {
                result.put(bars.get(i).getDate(), Double.NaN);
            } else {
                result.put(bars.get(i).getDate(), OBVs.subList(i - period, i).stream().mapToDouble(d -> d).average().orElse(Double.NaN));
            }
        }

        assert result.size() == bars.size();
        return result;
    }

    public int requiredSize() {
        return period;
    }
}
