package com.mztrade.hki.entity.indicator;

import com.mztrade.hki.entity.StockPrice;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleMovingAverageAlgorithm implements Algorithm {
    private final int period;

    public SimpleMovingAverageAlgorithm(List<Float> params) {
        this.period = params.getFirst().intValue();
    }

    public Map<LocalDateTime, Double> calculate(List<StockPrice> stockPrices) {
        Map<LocalDateTime, Double> result = new HashMap<>();

        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < period) {
                result.put(stockPrices.get(i).getDate(), Double.NaN);
            } else {
                result.put(stockPrices.get(i).getDate(), stockPrices.subList(i - period, i).stream().mapToInt(b -> b.getClose()).average().getAsDouble());
            }
        }

        assert result.size() == stockPrices.size();
        return result;
    }

    public int requiredSize() {
        return period;
    }
}
