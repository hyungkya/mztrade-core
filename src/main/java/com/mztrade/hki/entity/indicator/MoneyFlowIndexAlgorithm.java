package com.mztrade.hki.entity.indicator;

import com.mztrade.hki.entity.StockPrice;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoneyFlowIndexAlgorithm implements Algorithm {
    private final int period;
    public MoneyFlowIndexAlgorithm(List<Float> params) {
        this.period = params.getFirst().intValue();
    }

    public Map<LocalDateTime, Double> calculate(List<StockPrice> stockPrices) {
        Map<LocalDateTime, Double> result = new HashMap<>();

        List<Double> pRMF = new ArrayList<>();
        List<Double> nRMF = new ArrayList<>();
        for (int i = 1; i < stockPrices.size(); i++) {
            int previousTp = (stockPrices.get(i - 1).getHigh() + stockPrices.get(i - 1).getLow() + stockPrices.get(i - 1).getClose()) / 3;
            int tp = (stockPrices.get(i).getHigh() + stockPrices.get(i).getLow() + stockPrices.get(i).getClose()) / 3;
            double rmf = tp * stockPrices.get(i).getVolume();
            if (previousTp < tp) {
                pRMF.add(rmf);
                nRMF.add(0.0);
            } else {
                pRMF.add(0.0);
                nRMF.add(rmf);
            }
        }
        for (int i = 0; i < stockPrices.size(); i++) {
            if (i < period) {
                result.put(stockPrices.get(i).getDate(), Double.NaN);
            } else {
                double MFR = pRMF.subList(i - period, i).stream().mapToDouble(d -> d).sum() / nRMF.subList(i - period, i).stream().mapToDouble(d -> d).sum();
                result.put(stockPrices.get(i).getDate(), 100 - (100 / (1 + MFR)));
            }
        }
        assert result.size() == stockPrices.size();
        return result;
    }

    public int requiredSize() {
        return period;
    }
}
