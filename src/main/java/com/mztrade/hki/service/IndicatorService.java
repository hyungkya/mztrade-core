package com.mztrade.hki.service;

import com.mztrade.hki.entity.indicator.Indicator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IndicatorService {
    private final StockPriceService stockPriceService;

    @Autowired
    public IndicatorService(StockPriceService stockPriceService) {
        this.stockPriceService = stockPriceService;
    }

    public double getIndicator(String ticker, LocalDateTime date, String type, List<Float> params) {
        Indicator indicator = new Indicator()
                .setAlgorithm(type, params);
        indicator.setStockPrices(stockPriceService.getPrices(ticker, date.minusDays(indicator.requiredSize() * 2), date));
        return indicator.calculate().get(date);
    }

    public Map<LocalDateTime, Double> getIndicators(String ticker, LocalDateTime startDate, LocalDateTime endDate, String type, List<Float> params) {
        Indicator indicator = new Indicator()
                .setAlgorithm(type, params)
                .setStockPrices(stockPriceService.getPrices(ticker, startDate, endDate));
        return indicator.calculate();
    }
}
