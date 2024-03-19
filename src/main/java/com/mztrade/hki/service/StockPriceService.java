package com.mztrade.hki.service;

import com.mztrade.hki.Util;
import com.mztrade.hki.dto.StockInfoResponse;
import com.mztrade.hki.dto.StockPriceResponse;
import com.mztrade.hki.entity.StockPrice;
import com.mztrade.hki.entity.StockInfo;
import com.mztrade.hki.repository.StockInfoRepository;
import com.mztrade.hki.repository.StockInfoRepositoryImpl;
import com.mztrade.hki.repository.StockPriceRepository;
import com.mztrade.hki.repository.StockPriceRepositoryImpl;
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
    private final StockPriceRepositoryImpl stockPriceRepositoryImpl;
    private final StockInfoRepositoryImpl stockInfoRepositoryImpl;
    private final StockInfoRepository stockInfoRepository;
    private final StockPriceRepository stockPriceRepository;

    @Autowired
    public StockPriceService(StockPriceRepositoryImpl stockPriceRepositoryImpl,
                             StockInfoRepositoryImpl stockInfoRepositoryImpl, StockInfoRepository stockInfoRepository, StockPriceRepository stockPriceRepository) {
        this.stockPriceRepositoryImpl = stockPriceRepositoryImpl;
        this.stockInfoRepositoryImpl = stockInfoRepositoryImpl;
        this.stockInfoRepository = stockInfoRepository;
        this.stockPriceRepository = stockPriceRepository;
    }

    public List<StockPriceResponse> getPrices(String ticker) {
        List<StockPriceResponse> stockPriceResponses = stockPriceRepository.findByStockInfoTicker(ticker)
                .stream()
                .sorted(StockPrice.COMPARE_BY_DATE).map((s) -> StockPriceResponse.from(s))
                .collect(Collectors.toList());

        log.debug(String.format("[StockPriceService] getPrices(ticker: %s) -> List<bar>:%s", ticker, stockPriceResponses));
        // return all ticker data
        return stockPriceResponses;
    }

    public List<StockPrice> getPrices(String ticker, LocalDateTime startDate, LocalDateTime endDate) {
        List<StockPrice> stockPrice = stockPriceRepository.findByStockInfoTickerAndDateBetween(ticker, startDate, endDate);
        log.debug(String.format("[StockPriceService] getPrices(ticker: %s, startDate: %s, endDate: %s) -> List<Bar>:%s", ticker,startDate, endDate, stockPrice));
        // return requested date range's ticker data
        return stockPrice;
    }

    public StockPrice getPrice(String ticker, LocalDateTime date) {
        // return requested date's ticker data
        Optional<StockPrice> bar = stockPriceRepository.findByStockInfoTickerAndDate(ticker, date);
        if (bar.isEmpty()) {
            throw new EmptyResultDataAccessException(1);
        }
        log.debug(String.format("[StockPriceService] getPrice(ticker: %s, date: %s) -> Bar:%s", ticker,date, bar.get()));
        return bar.get();
    }

    public Optional<StockPrice> getAvailablePriceBefore(String ticker, LocalDateTime date) {
        Optional<StockPrice> bar = Optional.empty();
        while (date.isAfter(LocalDateTime.parse(Util.formatDate("20100101")))) {
            bar = stockPriceRepository.findByStockInfoTickerAndDate(ticker, date);
            if (bar.isPresent()) {
                break;
            } else {
                date = date.minus(1, ChronoUnit.DAYS);
            }
        }

        log.debug(String.format("[StockPriceService] getAvailablePriceBefore(ticker: %s, date: %s) -> Optional<Bar>:%s", ticker,date, bar));
        return bar;
    }

    public Optional<StockPrice> getAvailablePriceAfter(String ticker, LocalDateTime date) {
        Optional<StockPrice> bar = Optional.empty();
        while (date.isBefore(LocalDateTime.now())) {
            bar = stockPriceRepository.findByStockInfoTickerAndDate(ticker, date);
            if (bar.isPresent()) {
                break;
            } else {
                date = date.plus(1, ChronoUnit.DAYS);
            }
        }

        log.debug(String.format("[StockPriceService] getAvailablePriceAfter(ticker: %s, date: %s) -> Optional<Bar>:%s", ticker,date, bar));
        return bar;
    }

    public Optional<StockPrice> getAvailablePriceBefore(String ticker, LocalDateTime date, Integer maxRange) {
        Optional<StockPrice> bar = Optional.empty();
        for (; maxRange > 0; maxRange--) {
            bar = stockPriceRepository.findByStockInfoTickerAndDate(ticker, date);
            if (bar.isPresent()) {
                break;
            } else {
                date = date.minus(1, ChronoUnit.DAYS);
            }
        }

        log.debug(String.format("[StockPriceService] getAvailablePriceBefore(ticker: %s, date: %s, maxRange: %s) -> Optional<Bar>:%s", ticker,date,maxRange, bar));
        return bar;
    }

    public Optional<StockPrice> getAvailablePriceAfter(String ticker, LocalDateTime date, Integer maxRange) {
        Optional<StockPrice> bar = Optional.empty();
        for (; maxRange > 0; maxRange--) {
            bar = stockPriceRepository.findByStockInfoTickerAndDate(ticker, date);
            if (bar.isPresent()) {
                break;
            } else {
                date = date.plus(1, ChronoUnit.DAYS);
            }
        }
        log.debug(String.format("[StockPriceService] getAvailablePriceAfter(ticker: %s, date: %s, maxRange: %s) -> Optional<Bar>:%s", ticker,date,maxRange, bar));
        return bar;
    }

    public StockPrice getCurrentPrice(String ticker) {
        // return the most recent ticker data
        List<StockPrice> stockPrices = stockPriceRepository.findByStockInfoTicker(ticker);
        StockPrice stockPrice = stockPrices.get(stockPrices.size() - 1);
        log.debug(String.format("[StockPriceService] getCurrentPrice(ticker: %s) -> Bar:%s", ticker, stockPrice));
        return stockPrice;
    }

    public List<StockInfoResponse> getAllStockInfo() {
        List<StockInfoResponse> stockInfoResponses = stockInfoRepository.getAll().stream().map((s) -> StockInfoResponse.from(s)).toList();
        log.debug(String.format("[StockPriceService] getAllStockInfo() -> StockInfo:%s", stockInfoResponses));
        return stockInfoResponses;
    }

    public List<StockInfoResponse> searchStockInfoByName(String name) {
        List<StockInfoResponse> stockInfoResponses = stockInfoRepository.findAllByNameContainsIgnoreCase(name)
                .stream()
                .map((s) -> StockInfoResponse.from(s))
                .toList();
        log.debug(String.format("[StockPriceService] searchStockInfoByName(name: %s) -> StockInfo:%s", name, stockInfoResponses));
        return stockInfoResponses;
    }

    public StockInfoResponse findStockInfoByTicker(String ticker) {
        StockInfoResponse stockInfoResponse = StockInfoResponse.from(stockInfoRepository.findByTicker(ticker).get());
        log.debug(String.format("[StockPriceService] findStockInfoByTicker(ticker: %s) -> StockInfo:%s", ticker, stockInfoResponse));
        return stockInfoResponse;
    }
}
