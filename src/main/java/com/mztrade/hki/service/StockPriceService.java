package com.mztrade.hki.service;

import com.mztrade.hki.Util;
import com.mztrade.hki.dto.StockFinancialInfoResponse;
import com.mztrade.hki.dto.StockInfoResponse;
import com.mztrade.hki.dto.StockPriceResponse;
import com.mztrade.hki.entity.DailyStockPrice;
import com.mztrade.hki.entity.MinutelyStockPrice;
import com.mztrade.hki.repository.MinutelyStockPriceRepository;
import com.mztrade.hki.repository.StockInfoRepository;
import com.mztrade.hki.repository.DailyStockPriceRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StockPriceService {
    private final StockInfoRepository stockInfoRepository;
    private final DailyStockPriceRepository dailyStockPriceRepository;
    private final MinutelyStockPriceRepository minutelyStockPriceRepository;

    @Autowired
    public StockPriceService(StockInfoRepository stockInfoRepository, DailyStockPriceRepository dailyStockPriceRepository, MinutelyStockPriceRepository minutelyStockPriceRepository) {
        this.stockInfoRepository = stockInfoRepository;
        this.dailyStockPriceRepository = dailyStockPriceRepository;
        this.minutelyStockPriceRepository = minutelyStockPriceRepository;
    }

    public List<StockPriceResponse> getDailyPrices(String ticker) {
        List<StockPriceResponse> stockPriceResponses = dailyStockPriceRepository.findByStockInfoTicker(ticker)
                .stream()
                .sorted(DailyStockPrice.COMPARE_BY_DATE).map((s) -> StockPriceResponse.from(s))
                .collect(Collectors.toList());

        // return all ticker data
        return stockPriceResponses;
    }

    public List<StockPriceResponse> getMinutelyPrices(String ticker) {
        List<StockPriceResponse> stockPriceResponses = minutelyStockPriceRepository.findByStockInfoTicker(ticker)
                .stream()
                .sorted(MinutelyStockPrice.COMPARE_BY_DATE).map((s) -> StockPriceResponse.from(s))
                .collect(Collectors.toList());
        // return requested date range's ticker data
        return stockPriceResponses;
    }

    public List<DailyStockPrice> getDailyPrices(String ticker, LocalDateTime startDate, LocalDateTime endDate) {
        List<DailyStockPrice> dailyStockPrice = dailyStockPriceRepository.findByStockInfoTickerAndDateBetween(ticker, startDate, endDate);
        // return requested date range's ticker data
        return dailyStockPrice;
    }

    public List<MinutelyStockPrice> getMinutelyPrices(String ticker, LocalDateTime startDate, LocalDateTime endDate) {
        List<MinutelyStockPrice> minutelyStockPrices = minutelyStockPriceRepository.findByStockInfoTickerAndDateBetween(ticker, startDate, endDate);
        // return requested date range's ticker data
        return minutelyStockPrices;
    }

    public DailyStockPrice getDailyPrice(String ticker, LocalDateTime date) {
        // return requested date's ticker data
        Optional<DailyStockPrice> bar = dailyStockPriceRepository.findByStockInfoTickerAndDate(ticker, date);
        if (bar.isEmpty()) {
            throw new EmptyResultDataAccessException(1);
        }
        return bar.get();
    }

    public MinutelyStockPrice getMinutelyPrice(String ticker, LocalDateTime date) {
        Optional<MinutelyStockPrice> bar = minutelyStockPriceRepository.findByStockInfoTickerAndDate(ticker, date);
        if (bar.isEmpty()) {
            throw new EmptyResultDataAccessException(1);
        }
        return bar.get();
    }

    public Optional<DailyStockPrice> getAvailableDailyPriceBefore(String ticker, LocalDateTime date) {
        Optional<DailyStockPrice> bar = Optional.empty();
        while (date.isAfter(LocalDateTime.parse(Util.formatDate("20100101")))) {
            bar = dailyStockPriceRepository.findByStockInfoTickerAndDate(ticker, date);
            if (bar.isPresent()) {
                break;
            } else {
                date = date.minus(1, ChronoUnit.DAYS);
            }
        }
        return bar;
    }

    public Optional<MinutelyStockPrice> getAvailableMinutelyPriceBefore(String ticker, LocalDateTime date) {
        Optional<MinutelyStockPrice> bar = Optional.empty();
        while (date.isAfter(LocalDateTime.parse(Util.formatDate("202405200900")))) {
            bar = minutelyStockPriceRepository.findByStockInfoTickerAndDate(ticker, date);
            if (bar.isPresent()) {
                break;
            } else {
                if (date.getHour() == 9 && date.getMinute() == 0) {
                    date = date.minusDays(1).withHour(15).withMinute(30);
                } else {
                    date = date.minus(1, ChronoUnit.MINUTES);
                }
            }
        }
        return bar;
    }

    public Optional<DailyStockPrice> getAvailableDailyPriceBefore(String ticker, LocalDateTime date, Integer maxRange) {
        Optional<DailyStockPrice> bar = Optional.empty();
        for (; maxRange > 0; maxRange--) {
            bar = dailyStockPriceRepository.findByStockInfoTickerAndDate(ticker, date);
            if (bar.isPresent()) {
                break;
            } else {
                date = date.minus(1, ChronoUnit.DAYS);
            }
        }
        return bar;
    }

    public List<StockInfoResponse> getAllStockInfo() {
        List<StockInfoResponse> stockInfoResponses = stockInfoRepository.findAll().stream().map((s) -> StockInfoResponse.from(s)).toList();
        return stockInfoResponses;
    }

    public List<StockInfoResponse> searchStockInfoByName(String name) {
        List<StockInfoResponse> stockInfoResponses = stockInfoRepository.findAllByNameContainsIgnoreCase(name)
                .stream()
                .map((s) -> StockInfoResponse.from(s))
                .toList();
        return stockInfoResponses;
    }

    public StockInfoResponse findStockInfoByTicker(String ticker) {
        StockInfoResponse stockInfoResponse = StockInfoResponse.from(stockInfoRepository.findByTicker(ticker).get());
        return stockInfoResponse;
    }

    public StockFinancialInfoResponse findStockFinancialInfoByTicker(String ticker) {
        StockFinancialInfoResponse stockFinancialInfoResponse = StockFinancialInfoResponse.from(stockInfoRepository.getByTicker(ticker));
        return stockFinancialInfoResponse;
    }
}
