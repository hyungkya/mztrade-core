package com.mztrade.hki.entity.indicator;

import com.mztrade.hki.entity.StockPrice;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelativeStrengthIndexAlgorithm implements Algorithm {
    private final int period;

    public RelativeStrengthIndexAlgorithm(List<Float> params) {
        this.period = params.getFirst().intValue();
    }

    public Map<LocalDateTime, Double> calculate(List<StockPrice> stockPrices) {
        Map<LocalDateTime, Double> result = new HashMap<>();

        List<Integer> diffs = new ArrayList<>();
        for (int i = 1; i < stockPrices.size(); i++) {
            diffs.add(stockPrices.get(i).getClose() - stockPrices.get(i - 1).getClose());
        }
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < period) {
                result.put(stockPrices.get(i).getDate(), Double.NaN);
            } else {
                double au = diffs.subList(i - period, i).stream().filter(e -> e > 0).mapToInt(Math::abs).average().orElse(0);
                double ad = diffs.subList(i - period, i).stream().filter(e -> e < 0).mapToInt(Math::abs).average().orElse(100);
                result.put(stockPrices.get(i).getDate(), (au / (ad + au)) * 100);
            }
        }

        assert result.size() == stockPrices.size();
        return result;
    }

    public int requiredSize() {
        return period + 1;
    }
}
