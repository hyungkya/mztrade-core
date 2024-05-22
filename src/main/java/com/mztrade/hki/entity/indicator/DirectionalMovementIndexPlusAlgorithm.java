package com.mztrade.hki.entity.indicator;

import com.mztrade.hki.entity.StockPrice;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectionalMovementIndexPlusAlgorithm implements Algorithm {
    private final int period;

    public DirectionalMovementIndexPlusAlgorithm(List<Float> params) {
        this.period = params.getFirst().intValue();
    }

    public Map<LocalDateTime, Double> calculate(List<StockPrice> stockPrices) {
        Map<LocalDateTime, Double> result = new HashMap<>();

        List<Integer> PDMs = new ArrayList<>();
        List<Integer> MDMs = new ArrayList<>();
        List<Integer> TRs = new ArrayList<>();
        for (int i = 1; i < stockPrices.size(); i++) {
            int highDiff = Math.max(stockPrices.get(i).getHigh() - stockPrices.get(i - 1).getHigh(), 0);
            int lowDiff = Math.max(stockPrices.get(i - 1).getLow() - stockPrices.get(i).getLow(), 0);
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

            int a = Math.abs(stockPrices.get(i).getHigh() - stockPrices.get(i).getLow());
            int b = Math.abs(stockPrices.get(i).getHigh() - stockPrices.get(i - 1).getClose());
            int c = Math.abs(stockPrices.get(i).getLow() - stockPrices.get(i - 1).getClose());
            TRs.add(Math.max(Math.max(a, b), c));
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
        List<Double> PDM_EMA = new ArrayList<>();
        for (int i = 0; i < TRs.size(); i++) {
            if (i < period) {
                PDM_EMA.add(Double.NaN);
            } else if (i == period) {
                PDM_EMA.add(PDMs.subList(i - period, i).stream().mapToDouble(d -> d).average().orElse(0));
            } else {
                PDM_EMA.add(((period - 1) * PDM_EMA.getLast() + PDMs.get(i)) / period);
            }
        }
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < period) {
                result.put(stockPrices.get(i).getDate(), Double.NaN);
            } else {
                double pdi = PDM_EMA.get(i - 1) / TR_EMA.get(i - 1);
                result.put(stockPrices.get(i).getDate(), pdi);
            }
        }

        assert result.size() == stockPrices.size();
        return result;
    }

    public int requiredSize() {
        return period;
    }
}
