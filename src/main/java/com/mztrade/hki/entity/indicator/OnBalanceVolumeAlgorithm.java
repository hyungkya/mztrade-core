package com.mztrade.hki.entity.indicator;

import com.mztrade.hki.entity.Bar;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnBalanceVolumeAlgorithm implements Algorithm {

    public OnBalanceVolumeAlgorithm(List<Float> params) {}

    public Map<LocalDateTime, Double> calculate(List<? extends Bar> bars) {
        Map<LocalDateTime, Double> result = new HashMap<>();

        double OBV = 0;

        for (int i = 0; i < bars.size(); i++) {
            if (i == 0) {
                OBV = bars.get(i).getVolume();
                result.put(bars.get(i).getDate(), OBV);
            } else {
                if (bars.get(i).getClose() > bars.get(i - 1).getClose()) {
                    OBV += bars.get(i).getVolume();
                } else if (bars.get(i).getClose() < bars.get(i - 1).getClose()) {
                    OBV -= bars.get(i).getVolume();
                }
                result.put(bars.get(i).getDate(), OBV);
            }
        }

        assert result.size() == bars.size();
        return result;
    }

    public int requiredSize() {
        return 0;
    }
}
