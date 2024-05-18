package com.mztrade.hki.controller;

import com.mztrade.hki.Util;
import com.mztrade.hki.dto.StockFinancialInfoResponse;
import com.mztrade.hki.dto.StockInfoResponse;
import com.mztrade.hki.dto.StockPriceResponse;
import com.mztrade.hki.entity.backtest.PreviousIndicator;
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

import java.util.List;
import java.util.Optional;

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
    public ResponseEntity<List<StockPriceResponse>> getPricesByTicker(@PathVariable String ticker) {
        return new ResponseEntity<>(stockPriceService.getPrices(ticker), HttpStatus.OK);
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
            @RequestParam String type,
            @RequestParam List<Float> param
    ) {
        if (!endDate.isEmpty()) {
            return new ResponseEntity<>(
                    indicatorService.getIndicators(ticker, Util.stringToLocalDateTime(startDate),
                            Util.stringToLocalDateTime(endDate),
                            type, param), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                    indicatorService.getIndicator(ticker, Util.stringToLocalDateTime(startDate), type,
                            param), HttpStatus.OK);
        }
    }
}
