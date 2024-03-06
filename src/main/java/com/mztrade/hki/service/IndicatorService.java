package com.mztrade.hki.service;

import com.mztrade.hki.entity.Bar;
import com.mztrade.hki.entity.backtest.Indicator;
import com.mztrade.hki.repository.StockInfoRepository;
import com.mztrade.hki.repository.StockPriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class IndicatorService {
    private final StockPriceService stockPriceService;

    @Autowired
    public IndicatorService(StockPriceService stockPriceService) {
        this.stockPriceService = stockPriceService;
    }

    public Map<LocalDateTime, Double> getIndicator(String ticker, LocalDateTime startDate, LocalDateTime endDate, String type, List<Float> params) {
        List<Bar> bars = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();
        Indicator indicator = new Indicator(type, params);
        for (int i = 1; i <= bars.size(); i++) {
            result.put(bars.get(i - 1).getDate(), indicator.calculate(bars.subList(0, i)));
        }
        log.debug(String.format("[IndicatorService] getIndicator(ticker: %s, startDate: %s, endDate: %s, type: %s, params: %s) -> indicator:%s",
                ticker, startDate, endDate, type, params, result)
        );
        return result;
    }
}
