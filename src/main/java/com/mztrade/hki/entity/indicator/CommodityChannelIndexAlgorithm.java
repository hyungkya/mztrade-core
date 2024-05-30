package com.mztrade.hki.entity.indicator;

import static java.lang.Math.abs;

import com.mztrade.hki.entity.Bar;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommodityChannelIndexAlgorithm implements Algorithm {
    private final int n;
    public CommodityChannelIndexAlgorithm(List<Float> params) {
        this.n = params.getFirst().intValue();
    }

    public Map<LocalDateTime, Double> calculate(List<? extends Bar> bars) {
        List<Double> meanPrices = new ArrayList<>();

        for (int i = 0; i < bars.size(); i++) {
            meanPrices.add((bars.get(i).getHigh() + bars.get(i).getLow() + bars.get(i).getClose()) / 3.0);
        }
        List<Double> meanPricesMovingAverage = new ArrayList<>();
        for (int i = 0; i < bars.size(); i++) {
            if (i < n) {
                meanPricesMovingAverage.add(Double.NaN);
            } else {
                meanPricesMovingAverage.add(meanPrices.subList(i - n, i).stream().mapToDouble(d -> d).average().orElse(Double.NaN));
            }
        }
        List<Double> diffMovingAverage = new ArrayList<>();
        for (int i = 0; i < bars.size(); i++) {
            if (i < n) {
                diffMovingAverage.add(Double.NaN);
            } else {
                diffMovingAverage.add(meanPrices.get(i) - meanPricesMovingAverage.get(i));
            }
        }
        List<Double> absoluteDiffMovingAverage = new ArrayList<>();
        for (int i = 0; i < bars.size(); i++) {
            if (i < (n * 2) - 1) {
                absoluteDiffMovingAverage.add(Double.NaN);
            } else {
                absoluteDiffMovingAverage.add(diffMovingAverage.subList(i - n, i).stream().mapToDouble(d -> abs(d)).average().orElse(Double.NaN));
            }
        }
        Map<LocalDateTime, Double> result = new HashMap<>();
        for (int i = 0; i < bars.size(); i++) {
            if (i < (n * 2) - 1) {
                result.put(bars.get(i).getDate(), Double.NaN);
            } else {
                result.put(bars.get(i).getDate(), diffMovingAverage.get(i) / (absoluteDiffMovingAverage.get(i) * 0.015));
            }
        }
        assert result.size() == bars.size();
        return result;
    }
    public int requiredSize() {
        return n * 2;
    }
}
