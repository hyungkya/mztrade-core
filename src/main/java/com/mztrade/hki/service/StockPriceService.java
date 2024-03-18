package com.mztrade.hki.service;

import com.mztrade.hki.Util;
import com.mztrade.hki.entity.Bar;
import com.mztrade.hki.entity.StockInfo;
import com.mztrade.hki.repository.StockInfoRepository;
import com.mztrade.hki.repository.StockInfoRepositoryImpl;
import com.mztrade.hki.repository.StockPriceRepository;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StockPriceService {
    private final StockPriceRepository stockPriceRepository;
    private final StockInfoRepositoryImpl stockInfoRepositoryImpl;
    private final StockInfoRepository stockInfoRepository;

    @Autowired
    public StockPriceService(StockPriceRepository stockPriceRepository,
                             StockInfoRepositoryImpl stockInfoRepositoryImpl, StockInfoRepository stockInfoRepository) {
        this.stockPriceRepository = stockPriceRepository;
        this.stockInfoRepositoryImpl = stockInfoRepositoryImpl;
        this.stockInfoRepository = stockInfoRepository;
    }

    public List<Bar> getPrices(String ticker) {
        List<Bar> bar = stockPriceRepository.findByTicker(ticker)
                .stream()
                .sorted(Bar.COMPARE_BY_DATE).collect(Collectors.toList());

        log.debug(String.format("[StockPriceService] getPrices(ticker: %s) -> List<bar>:%s", ticker,bar));
        // return all ticker data
        return bar;
    }

    public List<Bar> getPrices(String ticker, LocalDateTime startDate, LocalDateTime endDate) {
        List<Bar> bar = stockPriceRepository.findByDate(ticker, startDate, endDate);
        log.debug(String.format("[StockPriceService] getPrices(ticker: %s, startDate: %s, endDate: %s) -> List<Bar>:%s", ticker,startDate, endDate,bar));
        // return requested date range's ticker data
        return bar;
    }

    public Bar getPrice(String ticker, LocalDateTime date) {
        // return requested date's ticker data
        Bar bar = stockPriceRepository.findByDate(ticker, date);
        log.debug(String.format("[StockPriceService] getPrice(ticker: %s, date: %s) -> Bar:%s", ticker,date, bar));
        return bar;
    }

    public Optional<Bar> getAvailablePriceBefore(String ticker, LocalDateTime date) {
        Optional<Bar> bar = Optional.empty();
        while (date.isAfter(LocalDateTime.parse(Util.formatDate("20100101")))) {
            try {
                bar = Optional.of(stockPriceRepository.findByDate(ticker, date));
                break;
            } catch (EmptyResultDataAccessException ignored) {
                date = date.minus(1, ChronoUnit.DAYS);
            }
        }

        log.debug(String.format("[StockPriceService] getAvailablePriceBefore(ticker: %s, date: %s) -> Optional<Bar>:%s", ticker,date, bar));
        return bar;
    }

    public Optional<Bar> getAvailablePriceAfter(String ticker, LocalDateTime date) {
        Optional<Bar> bar = Optional.empty();
        while (date.isBefore(LocalDateTime.now())) {
            try {
                bar = Optional.of(stockPriceRepository.findByDate(ticker, date));
                break;
            } catch (EmptyResultDataAccessException ignored) {
                date = date.plus(1, ChronoUnit.DAYS);
            }
        }

        log.debug(String.format("[StockPriceService] getAvailablePriceAfter(ticker: %s, date: %s) -> Optional<Bar>:%s", ticker,date, bar));
        return bar;
    }

    public Optional<Bar> getAvailablePriceBefore(String ticker, LocalDateTime date, Integer maxRange) {
        Optional<Bar> bar = Optional.empty();
        for (; maxRange > 0; maxRange--) {
            try {
                bar = Optional.of(stockPriceRepository.findByDate(ticker, date));
                break;
            } catch (EmptyResultDataAccessException ignored) {
                date = date.minus(1, ChronoUnit.DAYS);
            }
        }

        log.debug(String.format("[StockPriceService] getAvailablePriceBefore(ticker: %s, date: %s, maxRange: %s) -> Optional<Bar>:%s", ticker,date,maxRange, bar));
        return bar;
    }

    public Optional<Bar> getAvailablePriceAfter(String ticker, LocalDateTime date, Integer maxRange) {
        Optional<Bar> bar = Optional.empty();
        for (; maxRange > 0; maxRange--) {
            try {
                bar = Optional.of(stockPriceRepository.findByDate(ticker, date));
                break;
            } catch (EmptyResultDataAccessException ignored) {
                date = date.plus(1, ChronoUnit.DAYS);
            }
        }
        log.debug(String.format("[StockPriceService] getAvailablePriceAfter(ticker: %s, date: %s, maxRange: %s) -> Optional<Bar>:%s", ticker,date,maxRange, bar));
        return bar;
    }

    public Bar getCurrentPrice(String ticker) {
        // return the most recent ticker data
        List<Bar> bars = getPrices(ticker);
        Bar bar = bars.get(bars.size() - 1);
        log.debug(String.format("[StockPriceService] getCurrentPrice(ticker: %s) -> Bar:%s", ticker,bar));
        return bar;
    }

    public List<StockInfo> getAllStockInfo() {
        List<StockInfo> stockInfos = stockInfoRepository.getAll();
        log.debug(String.format("[StockPriceService] getAllStockInfo() -> StockInfo:%s", stockInfos));
        return stockInfos;
    }

    public List<StockInfo> searchStockInfoByName(String name) {
        List<StockInfo> stockInfos = stockInfoRepository.findAllByNameContainsIgnoreCase(name);
        log.debug(String.format("[StockPriceService] searchStockInfoByName(name: %s) -> StockInfo:%s", name, stockInfos));
        return stockInfos;
    }

    public StockInfo findStockInfoByTicker(String ticker) {
        StockInfo stockInfo = stockInfoRepository.findByTicker(ticker).get();
        log.debug(String.format("[StockPriceService] findStockInfoByTicker(ticker: %s) -> StockInfo:%s", ticker, stockInfo));
        return stockInfo;
    }
}
