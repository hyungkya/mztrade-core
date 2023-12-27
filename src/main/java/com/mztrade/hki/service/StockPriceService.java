package com.mztrade.hki.service;

import com.mztrade.hki.entity.Bar;
import com.mztrade.hki.entity.StockInfo;
import com.mztrade.hki.entity.backtest.Indicator;
import com.mztrade.hki.entity.backtest.IndicatorBar;
import com.mztrade.hki.repository.StockInfoRepository;
import com.mztrade.hki.repository.StockPriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StockPriceService {
    private final StockPriceRepository stockPriceRepository;
    private final StockInfoRepository stockInfoRepository;

    @Autowired
    public StockPriceService(StockPriceRepository stockPriceRepository,
                             StockInfoRepository stockInfoRepository) {
        this.stockPriceRepository = stockPriceRepository;
        this.stockInfoRepository = stockInfoRepository;
    }

    public List<Bar> getPrices(String ticker) {
        // return all ticker data
        return stockPriceRepository.findByTicker(ticker)
                .stream()
                .sorted(Bar.COMPARE_BY_DATE).collect(Collectors.toList());
    }

    public List<Bar> getPrices(String ticker, Instant startDate, Instant endDate) {
        // return requested date range's ticker data
        return stockPriceRepository.findByDate(ticker, startDate, endDate);
    }

    public Bar getPrice(String ticker, Instant date) {
        // return requested date's ticker data
        return stockPriceRepository.findByDate(ticker, date);
    }

    public Bar getCurrentPrice(String ticker) {
        // return the most recent ticker data
        List<Bar> bars = getPrices(ticker);
        return bars.get(bars.size() - 1);
    }

    public List<StockInfo> getAllStockInfo() {
        return stockInfoRepository.getAll();
    }

    public Map<Instant, Double> getIndicator(String ticker, String type, List<Float> params) {
        List<Bar> bars = stockPriceRepository.findByTicker(ticker);
        Map<Instant, Double> result = new HashMap<>();
        Indicator indicator = new Indicator(type, params);
        for (int i = 1; i <= bars.size(); i++) {
            result.put(bars.get(i-1).getDate(), indicator.calculate(bars.subList(0, i)));
        }
        return result;
    }
}
