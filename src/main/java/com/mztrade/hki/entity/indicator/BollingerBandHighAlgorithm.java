package com.mztrade.hki.entity.indicator;

import com.mztrade.hki.entity.StockPrice;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BollingerBandHighAlgorithm implements Algorithm {
    private final int period;
    private final int exp;

    public BollingerBandHighAlgorithm(List<Float> params) {
        this.period = params.getFirst().intValue();
        this.exp = params.get(1).intValue();
    }

    public Map<LocalDateTime, Double> calculate(List<StockPrice> stockPrices) {
        Map<LocalDateTime, Double> result = new HashMap<>();

        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < period) {
                result.put(stockPrices.get(i).getDate(), Double.NaN);
            } else {
                double avg = stockPrices.subList(i + 1 - period, i + 1)
                        .stream()
                        .mapToInt(StockPrice::getClose)
                        .average()
                        .orElseThrow();
                double squareSum = stockPrices.subList(i + 1 - period, i + 1)
                        .stream()
                        .mapToDouble(b -> Math.pow(Math.abs(b.getClose() - avg), 2))
                        .sum();
                double std = Math.sqrt(squareSum / (period - 1));
                result.put(stockPrices.get(i).getDate(), avg + (std * exp));
            }
        }

        assert result.size() == stockPrices.size();
        return result;
    }
    public int requiredSize() {
        return period;
    }
}
