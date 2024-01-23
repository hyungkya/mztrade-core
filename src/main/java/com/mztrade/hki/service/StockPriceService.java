package com.mztrade.hki.service;

import com.mztrade.hki.Util;
import com.mztrade.hki.entity.Bar;
import com.mztrade.hki.entity.StockInfo;
import com.mztrade.hki.entity.backtest.Indicator;
import com.mztrade.hki.repository.StockInfoRepository;
import com.mztrade.hki.repository.StockPriceRepository;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.time.Instant;
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

    public Optional<Bar> getAvailablePriceBefore(String ticker, Instant date) {
        while (date.isBefore(Instant.parse(Util.formatDate("20100101")))) {
            try {
                return Optional.of(stockPriceRepository.findByDate(ticker, date));
            } catch (EmptyResultDataAccessException ignored) {
                date = date.minus(1, ChronoUnit.DAYS);
            }
        }
        return Optional.empty();
    }

    public Optional<Bar> getAvailablePriceAfter(String ticker, Instant date) {
        while (date.isBefore(Instant.now())) {
            try {
                return Optional.of(stockPriceRepository.findByDate(ticker, date));
            } catch (EmptyResultDataAccessException ignored) {
                date = date.plus(1, ChronoUnit.DAYS);
            }
        }
        return Optional.empty();
    }

    public Optional<Bar> getAvailablePriceBefore(String ticker, Instant date, Integer maxRange) {
        for (; maxRange > 0; maxRange--) {
            try {
                return Optional.of(stockPriceRepository.findByDate(ticker, date));
            } catch (EmptyResultDataAccessException ignored) {
                date = date.minus(1, ChronoUnit.DAYS);
            }
        }
        return Optional.empty();
    }

    public Optional<Bar> getAvailablePriceAfter(String ticker, Instant date, Integer maxRange) {
        for (; maxRange > 0; maxRange--) {
            try {
                return Optional.of(stockPriceRepository.findByDate(ticker, date));
            } catch (EmptyResultDataAccessException ignored) {
                date = date.plus(1, ChronoUnit.DAYS);
            }
        }
        return Optional.empty();
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
