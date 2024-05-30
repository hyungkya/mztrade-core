package com.mztrade.hki.controller;

import com.mztrade.hki.Util;
import com.mztrade.hki.dto.StockFinancialInfoResponse;
import com.mztrade.hki.dto.StockInfoResponse;
import com.mztrade.hki.dto.StockPriceResponse;
import com.mztrade.hki.entity.Bar;
import com.mztrade.hki.service.IndicatorService;
import com.mztrade.hki.service.StockPriceService;
import com.mztrade.hki.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@RestController
@Slf4j
public class StockController {
    private final StockPriceService stockPriceService;
    private final TagService tagService;
    private final IndicatorService indicatorService;

    public StockController(StockPriceService stockPriceService, TagService tagService, IndicatorService indicatorService) {
        this.stockPriceService = stockPriceService;
        this.tagService = tagService;
        this.indicatorService = indicatorService;
    }

    @GetMapping("/stock")
    public ResponseEntity<List<StockInfoResponse>> findStockInfo(
            @RequestParam(defaultValue = "") String name,
            @RequestParam Optional<List<Integer>> tids,
            @RequestParam Optional<Integer> uid
    ) {
        HttpStatus httpStatus = HttpStatus.OK;
        List<StockInfoResponse> stockInfoResponses = null;
        if (tids.isPresent() && !tids.isEmpty()) {
            if (uid.isPresent()) {
                stockInfoResponses = tagService.findStockInfoByNameAndTags(uid.get(), name, tids.get());
            } else {
                httpStatus = HttpStatus.BAD_REQUEST;
            }
        } else if (!name.isEmpty()) {
            stockInfoResponses = stockPriceService.searchStockInfoByName(name);
        } else {
            stockInfoResponses = stockPriceService.getAllStockInfo();
        }
        return new ResponseEntity<>(stockInfoResponses, httpStatus);
    }

    @GetMapping("/stock/{ticker}/price")
    public ResponseEntity<List<StockPriceResponse>> getPricesByTicker(
            @PathVariable String ticker,
            @RequestParam(defaultValue = "day") String option
    ) {
        if (option.equals("minute")) {
            return new ResponseEntity<>(stockPriceService.getMinutelyPrices(ticker), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(stockPriceService.getDailyPrices(ticker), HttpStatus.OK);
        }
    }

    @GetMapping("/stock/{ticker}/info")
    public ResponseEntity<StockInfoResponse> getStockInfoByTicker(
            @PathVariable String ticker) {
        StockInfoResponse stockInfoResponse = stockPriceService.findStockInfoByTicker(ticker);
        return new ResponseEntity<>(stockInfoResponse, HttpStatus.OK);
    }

    @GetMapping("/stock/{ticker}/financial-info")
    public ResponseEntity<StockFinancialInfoResponse> getStockFinancialInfoByTicker(
            @PathVariable String ticker) {
        StockFinancialInfoResponse stockFinancialInfoResponse = stockPriceService.findStockFinancialInfoByTicker(
                ticker);
        return new ResponseEntity<>(stockFinancialInfoResponse, HttpStatus.OK);
    }

    @GetMapping("/stock/{ticker}/indicator")
    public ResponseEntity<?> getSingleIndicatorByTicker(
            @PathVariable String ticker,
            @RequestParam String startDate,
            @RequestParam(defaultValue = "") String endDate,
            @RequestParam(defaultValue = "day") String option,
            @RequestParam String type,
            @RequestParam List<Float> param
    ) {
        LocalDateTime start = Util.stringToLocalDateTime(startDate);
        if (!endDate.isEmpty()) {
            List<? extends Bar> stockPrices;
            if (option.equals("minute")) {
                stockPrices = stockPriceService.getMinutelyPrices(ticker, start, Util.stringToLocalDateTime(endDate));
            } else {
                stockPrices = stockPriceService.getDailyPrices(ticker, start, Util.stringToLocalDateTime(endDate));
            }
            return new ResponseEntity<>(
                    indicatorService.getIndicators(stockPrices, type, param), HttpStatus.OK);
        } else {
            List<? extends Bar> stockPrices;
            if (option.equals("minute")) {
                LocalDateTime requiredDate = start;
                for (int i = 0; i < indicatorService.requiredBars(type, param) * 2; i++) {
                    if (requiredDate.getHour() == 9 && requiredDate.getMinute() == 0) {
                        requiredDate = requiredDate.minusDays(1).withHour(15).withMinute(30);
                    } else {
                        requiredDate = requiredDate.minusMinutes(1);
                    }
                }
                stockPrices = stockPriceService.getMinutelyPrices(ticker, requiredDate, start);
            } else {
                stockPrices = stockPriceService.getDailyPrices(
                        ticker, start.minusDays(indicatorService.requiredBars(type, param) * 2), start);
            }
            return new ResponseEntity<>(
                    indicatorService.getIndicator(
                            stockPrices,
                            Util.stringToLocalDateTime(startDate),
                            type,
                            param),
                    HttpStatus.OK);
        }
    }
}
