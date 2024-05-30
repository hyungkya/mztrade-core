package com.mztrade.hki.entity.indicator;

import com.mztrade.hki.entity.Bar;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoneyFlowIndexAlgorithm implements Algorithm {
    private final int period;
    public MoneyFlowIndexAlgorithm(List<Float> params) {
        this.period = params.getFirst().intValue();
    }

    public Map<LocalDateTime, Double> calculate(List<? extends Bar> bars) {
        Map<LocalDateTime, Double> result = new HashMap<>();

        List<Double> pRMF = new ArrayList<>();
        List<Double> nRMF = new ArrayList<>();
        for (int i = 1; i < bars.size(); i++) {
            int previousTp = (bars.get(i - 1).getHigh() + bars.get(i - 1).getLow() + bars.get(i - 1).getClose()) / 3;
            int tp = (bars.get(i).getHigh() + bars.get(i).getLow() + bars.get(i).getClose()) / 3;
            double rmf = tp * bars.get(i).getVolume();
            if (previousTp < tp) {
                pRMF.add(rmf);
                nRMF.add(0.0);
            } else {
                pRMF.add(0.0);
                nRMF.add(rmf);
            }
        }
        for (int i = 0; i < bars.size(); i++) {
            if (i < period) {
                result.put(bars.get(i).getDate(), Double.NaN);
            } else {
                double MFR = pRMF.subList(i - period, i).stream().mapToDouble(d -> d).sum() / nRMF.subList(i - period, i).stream().mapToDouble(d -> d).sum();
                result.put(bars.get(i).getDate(), 100 - (100 / (1 + MFR)));
            }
        }
        assert result.size() == bars.size();
        return result;
    }

    public int requiredSize() {
        return period;
    }
}
