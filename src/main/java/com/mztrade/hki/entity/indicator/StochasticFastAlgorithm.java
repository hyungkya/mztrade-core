package com.mztrade.hki.entity.indicator;

import com.mztrade.hki.entity.Bar;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StochasticFastAlgorithm implements Algorithm {
    private final int n;
    private final int m;

    public StochasticFastAlgorithm(List<Float> params) {
        this.n = params.getFirst().intValue();
        this.m = params.get(1).intValue();
    }

    public Map<LocalDateTime, Double> calculate(List<? extends Bar> bars) {
        Map<LocalDateTime, Double> result = new HashMap<>();


        List<Double> percentKs = new ArrayList<>();
        for (int i = 0; i < bars.size(); i++) {
            if (i < n) {
                percentKs.add(Double.NaN);
            } else {
                int highestPrice = bars.subList(i + 1 - n, i + 1).stream().reduce((acc, c) -> acc.getHigh() > c.getHigh() ? acc : c).orElseThrow().getHigh();
                int lowestPrice = bars.subList(i + 1 - n, i + 1).stream().reduce((acc, c) -> acc.getLow() < c.getLow() ? acc : c).orElseThrow().getLow();
                double percentK = ((double) (bars.get(i).getClose() - lowestPrice) / (highestPrice - lowestPrice)) * 100;
                percentKs.add(percentK);
            }
        }

        for (int i = 0; i < bars.size(); i++) {
            if (i < n + m) {
                result.put(bars.get(i).getDate(), Double.NaN);
            } else {
                result.put(bars.get(i).getDate(), percentKs.subList(i + 1 - m, i + 1).stream().mapToDouble(e -> e).average().orElse(Double.NaN));
            }
        }
        assert bars.size() == result.size();
        return result;
    }

    public int requiredSize() {
        return n + m;
    }
}
