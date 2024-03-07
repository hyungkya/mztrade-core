package com.mztrade.hki.service;

import com.mztrade.hki.entity.Bar;
import com.mztrade.hki.entity.backtest.Indicator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class IndicatorService {
    private final StockPriceService stockPriceService;

    @Autowired
    public IndicatorService(StockPriceService stockPriceService) {
        this.stockPriceService = stockPriceService;
    }

    public double getIndicator(String ticker, LocalDateTime date, String type, List<Float> params) {
        int maxRange = 1;
        if (!params.isEmpty()) {
            for (Float value : params) {
                maxRange += value.intValue();
            }
        }
        System.out.println(params);
        System.out.println(maxRange);
        List<Bar> bars = new ArrayList<>();
        int maxFail = 10;
        while (bars.size() < maxRange && maxFail > 0) {
            try {
                bars.add(stockPriceService.getPrice(ticker, date));
                maxFail = 10;
            } catch(DataAccessException e) {
                maxFail--;
            }
            date = date.minus(1, ChronoUnit.DAYS);
        }
        Indicator indicator = new Indicator(type, params);
        double result = indicator.calculate(bars);

        log.debug(String.format("[IndicatorService] getIndicator(ticker: %s, date: %s, type: %s, params: %s) -> indicator:%s",
                ticker, date, type, params, result)
        );
        return indicator.calculate(bars);
    }
    public Map<LocalDateTime, Double> getIndicators(String ticker, LocalDateTime startDate, LocalDateTime endDate, String type, List<Float> params) {
        List<Bar> bars = stockPriceService.getPrices(ticker, startDate, endDate);
        Map<LocalDateTime, Double> result = new HashMap<>();
        Indicator indicator = new Indicator(type, params);
        for (int i = 1; i <= bars.size(); i++) {
            result.put(bars.get(i - 1).getDate(), indicator.calculate(bars.subList(0, i)));
        }
        log.debug(String.format("[IndicatorService] getIndicators(ticker: %s, startDate: %s, endDate: %s, type: %s, params: %s) -> indicator:%s",
                ticker, startDate, endDate, type, params, result)
        );
        return result;
    }
}
