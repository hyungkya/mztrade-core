package com.mztrade.hki.entity.indicator;

import com.mztrade.hki.entity.StockPrice;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExponentialMovingAverageAlgorithm implements Algorithm {
    private final int period;

    public ExponentialMovingAverageAlgorithm(List<Float> params) {
        this.period = params.getFirst().intValue();
    }

    public Map<LocalDateTime, Double> calculate(List<StockPrice> stockPrices) {
        Map<LocalDateTime, Double> result = new HashMap<>();

        double previousEMA = Double.NaN;
        double smoothingConstant = 1 - (2.0 / (period + 1));
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < period) {
                result.put(stockPrices.get(i).getDate(), Double.NaN);
            } else if (i == period) {
                previousEMA = stockPrices.subList(i - period, i).stream().mapToInt(b -> b.getClose()).average().getAsDouble();
                result.put(stockPrices.get(i).getDate(), previousEMA);
            } else {
                previousEMA = (smoothingConstant * (stockPrices.get(i).getClose() - previousEMA)) + previousEMA;
                result.put(stockPrices.get(i).getDate(), previousEMA);
            }
        }

        assert result.size() == stockPrices.size();
        return result;
    }

    public int requiredSize() {
        return period;
    }
}
