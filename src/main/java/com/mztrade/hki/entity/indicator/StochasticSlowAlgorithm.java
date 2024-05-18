package com.mztrade.hki.entity.indicator;

import com.mztrade.hki.entity.StockPrice;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StochasticSlowAlgorithm implements Algorithm {
    private final int n;
    private final int m;
    private final int t;
    public StochasticSlowAlgorithm(List<Float> params) {
        this.n = params.getFirst().intValue();
        this.m = params.get(1).intValue();
        this.t = params.get(2).intValue();
    }

    public Map<LocalDateTime, Double> calculate(List<StockPrice> stockPrices) {
        Map<LocalDateTime, Double> result = new HashMap<>();

        List<Double> percentKs = new ArrayList<>();
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < n) {
                percentKs.add(Double.NaN);
            } else {
                int highestPrice = stockPrices.subList(i + 1 - n, i + 1).stream().reduce((acc, c) -> acc.getHigh() > c.getHigh() ? acc : c).orElseThrow().getHigh();
                int lowestPrice = stockPrices.subList(i + 1 - n, i + 1).stream().reduce((acc, c) -> acc.getLow() < c.getLow() ? acc : c).orElseThrow().getLow();
                double percentK = ((double) (stockPrices.get(i).getClose() - lowestPrice) / (highestPrice - lowestPrice)) * 100;
                percentKs.add(percentK);
            }
        }
        List<Double> percentDs = new ArrayList<>();
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < n + m) {
                percentDs.add(Double.NaN);
            } else {
                percentDs.add(percentKs.subList(i + 1 - m, i + 1).stream().mapToDouble(e -> e).average().orElse(Double.NaN));
            }
        }

        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < n + m + t) {
                result.put(stockPrices.get(i).getDate(), Double.NaN);
            } else {
                result.put(stockPrices.get(i).getDate(), percentDs.subList(i + 1 - t, i + 1).stream().mapToDouble(e -> e).average().orElse(Double.NaN));
            }
        }
        assert stockPrices.size() == result.size();
        return result;
    }

    public int requiredSize() {
        return n + m + t;
    }
}
