package com.mztrade.hki.entity.indicator;

import com.mztrade.hki.entity.StockPrice;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PivotPointResist2Algorithm implements Algorithm {
    private final int period;

    public PivotPointResist2Algorithm(List<Float> params) {
        this.period = params.getFirst().intValue();
    }

    public Map<LocalDateTime, Double> calculate(List<StockPrice> stockPrices) {
        Map<LocalDateTime, Double> result = new HashMap<>();

        int currentDuration = 0;
        double r2 = Double.NaN;
        for (int i = 0; i < stockPrices.size(); i += period) {
            int high = 0;
            int low = 0;
            int close = stockPrices.subList(i, i + period <= stockPrices.size() ? i + period : stockPrices.size()).getLast().getClose();
            for (int j = i; j < i + period && j < stockPrices.size(); j++) {
                high = stockPrices.get(j).getHigh() > high ? stockPrices.get(j).getHigh() : high;
                low = low == 0 || stockPrices.get(j).getLow() < low ? stockPrices.get(j).getLow() : low;
                result.put(stockPrices.get(j).getDate(), r2);
            }
            double pp = (high + low + close) / 3;
            r2 = pp + (high - low);
        }

        assert result.size() == stockPrices.size();
        return result;
    }

    public int requiredSize() {
        return period;
    }
}
