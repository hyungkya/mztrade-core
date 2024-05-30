package com.mztrade.hki.entity.indicator;

import com.mztrade.hki.entity.Bar;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectionalMovementIndexMinusAlgorithm implements Algorithm {
    private final int period;

    public DirectionalMovementIndexMinusAlgorithm(List<Float> params) {
        this.period = params.getFirst().intValue();
    }

    public Map<LocalDateTime, Double> calculate(List<? extends Bar> bars) {
        Map<LocalDateTime, Double> result = new HashMap<>();

        List<Integer> PDMs = new ArrayList<>();
        List<Integer> MDMs = new ArrayList<>();
        List<Integer> TRs = new ArrayList<>();
        for (int i = 1; i < bars.size(); i++) {
            int highDiff = Math.max(bars.get(i).getHigh() - bars.get(i - 1).getHigh(), 0);
            int lowDiff = Math.max(bars.get(i - 1).getLow() - bars.get(i).getLow(), 0);
            if (highDiff > lowDiff) {
                PDMs.add(highDiff);
                MDMs.add(0);
            } else if (lowDiff > highDiff) {
                PDMs.add(0);
                MDMs.add(lowDiff);
            } else {
                PDMs.add(0);
                MDMs.add(0);
            }

            int a = Math.abs(bars.get(i).getHigh() - bars.get(i).getLow());
            int b = Math.abs(bars.get(i).getHigh() - bars.get(i - 1).getClose());
            int c = Math.abs(bars.get(i).getLow() - bars.get(i - 1).getClose());
            int aa = Math.max(a, b);
            TRs.add(Math.max(aa, c));
        }
        List<Double> TR_EMA = new ArrayList<>();
        for (int i = 0; i < TRs.size(); i++) {
            if (i < period) {
                TR_EMA.add(Double.NaN);
            } else if (i == period) {
                TR_EMA.add(TRs.subList(i - period, i).stream().mapToDouble(d -> d).average().orElse(0));
            } else {
                TR_EMA.add(((period - 1) * TR_EMA.getLast() + TRs.get(i)) / period);
            }
        }
        List<Double> MDM_EMA = new ArrayList<>();
        for (int i = 0; i < TRs.size(); i++) {
            if (i < period) {
                MDM_EMA.add(Double.NaN);
            } else if (i == period) {
                MDM_EMA.add(MDMs.subList(i - period, i).stream().mapToDouble(d -> d).average().orElse(0));
            } else {
                MDM_EMA.add(((period - 1) * MDM_EMA.getLast() + MDMs.get(i)) / period);
            }
        }
        for (int i = 0; i < bars.size(); i++) {
            if (i < period) {
                result.put(bars.get(i).getDate(), Double.NaN);
            } else {
                double mdi = MDM_EMA.get(i - 1) / TR_EMA.get(i - 1);
                result.put(bars.get(i).getDate(), mdi);
            }
        }

        assert result.size() == bars.size();
        return result;
    }

    public int requiredSize() {
        return period;
    }
}
