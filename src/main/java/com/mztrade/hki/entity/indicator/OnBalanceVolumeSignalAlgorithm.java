package com.mztrade.hki.entity.indicator;

import com.mztrade.hki.entity.StockPrice;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnBalanceVolumeSignalAlgorithm implements Algorithm {
    private final int period;

    public OnBalanceVolumeSignalAlgorithm(List<Float> params) {
        this.period = params.getFirst().intValue();

    }

    public Map<LocalDateTime, Double> calculate(List<StockPrice> stockPrices) {
        Map<LocalDateTime, Double> result = new HashMap<>();

        double OBV = 0;
        List<Double> OBVs = new ArrayList<>();
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i == 0) {
                OBV = stockPrices.get(i).getVolume();
                OBVs.add(OBV);
            } else {
                if (stockPrices.get(i).getClose() > stockPrices.get(i - 1).getClose()) {
                    OBV += stockPrices.get(i).getVolume();
                } else if (stockPrices.get(i).getClose() < stockPrices.get(i - 1).getClose()) {
                    OBV -= stockPrices.get(i).getVolume();
                }
                OBVs.add(OBV);
            }
        }

        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < period) {
                result.put(stockPrices.get(i).getDate(), Double.NaN);
            } else {
                result.put(stockPrices.get(i).getDate(), OBVs.subList(i - period, i).stream().mapToDouble(d -> d).average().orElse(Double.NaN));
            }
        }

        assert result.size() == stockPrices.size();
        return result;
    }

    public int requiredSize() {
        return period;
    }
}
