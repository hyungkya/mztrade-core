package com.mztrade.hki.entity.indicator;

import com.mztrade.hki.entity.StockPrice;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovingAverageConvergenceDivergenceAlgorithm implements Algorithm {
    private final int shortPeriod;
    private final int longPeriod;

    public MovingAverageConvergenceDivergenceAlgorithm(List<Float> params) {
        this.shortPeriod = params.getFirst().intValue();
        this.longPeriod = params.get(1).intValue();
    }

    public Map<LocalDateTime, Double> calculate(List<StockPrice> stockPrices) {
        Map<LocalDateTime, Double> result = new HashMap<>();

        List<Double> shortEMA = new ArrayList<>();
        double previousEMA = Double.NaN;
        double smoothingConstant = 1 - (2.0 / (shortPeriod + 1));
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < shortPeriod) {
                shortEMA.add(Double.NaN);
            } else if (i == shortPeriod) {
                previousEMA = stockPrices.subList(i - shortPeriod, i).stream().mapToInt(b -> b.getClose()).average().getAsDouble();
                shortEMA.add(previousEMA);
            } else {
                previousEMA = (smoothingConstant * (stockPrices.get(i).getClose() - previousEMA)) + previousEMA;
                shortEMA.add(previousEMA);
            }
        }

        List<Double> longEMA = new ArrayList<>();
        previousEMA = Double.NaN;
        smoothingConstant = 1 - (2.0 / (longPeriod + 1));
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < longPeriod) {
                longEMA.add(Double.NaN);
            } else if (i == longPeriod) {
                previousEMA = stockPrices.subList(i - longPeriod, i).stream().mapToInt(b -> b.getClose()).average().getAsDouble();
                longEMA.add(previousEMA);
            } else {
                previousEMA = (smoothingConstant * (stockPrices.get(i).getClose() - previousEMA)) + previousEMA;
                longEMA.add(previousEMA);
            }
        }

        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < longPeriod) {
                result.put(stockPrices.get(i).getDate(), Double.NaN);
            } else {
                result.put(stockPrices.get(i).getDate(), longEMA.get(i) - shortEMA.get(i));
            }
        }
        assert result.size() == stockPrices.size();
        return result;
    }

    public int requiredSize() {
        return longPeriod;
    }
}
