package com.mztrade.hki.entity.indicator;

import com.mztrade.hki.entity.StockPrice;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnBalanceVolumeAlgorithm implements Algorithm {

    public OnBalanceVolumeAlgorithm(List<Float> params) {}

    public Map<LocalDateTime, Double> calculate(List<StockPrice> stockPrices) {
        Map<LocalDateTime, Double> result = new HashMap<>();

        double OBV = 0;

        for (int i = 0; i < stockPrices.size(); i++) {
            if (i == 0) {
                OBV = stockPrices.get(i).getVolume();
                result.put(stockPrices.get(i).getDate(), OBV);
            } else {
                if (stockPrices.get(i).getClose() > stockPrices.get(i - 1).getClose()) {
                    OBV += stockPrices.get(i).getVolume();
                } else if (stockPrices.get(i).getClose() < stockPrices.get(i - 1).getClose()) {
                    OBV -= stockPrices.get(i).getVolume();
                }
                result.put(stockPrices.get(i).getDate(), OBV);
            }
        }

        assert result.size() == stockPrices.size();
        return result;
    }

    public int requiredSize() {
        return 0;
    }
}
