package com.mztrade.hki.controller;

import com.mztrade.hki.dto.StockFinancialInfoResponse;
import com.mztrade.hki.dto.StockInfoResponse;
import com.mztrade.hki.dto.StockPriceResponse;
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

    public StockController(StockPriceService stockPriceService, TagService tagService) {
        this.stockPriceService = stockPriceService;
        this.tagService = tagService;
    }

    @GetMapping("/stock")
    public ResponseEntity<List<StockInfoResponse>> findStockInfo(
            @RequestParam(defaultValue = "") String name,
            @RequestParam Optional<List<Integer>> tids,
            @RequestParam Optional<Integer> uid
    ) {
        log.info(String.format("[GET] /stock?name=%s&tids=%s&uid=%s", name, tids, uid));
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
        log.info(String.format("[GET] /stock/%s/price", ticker));
        return new ResponseEntity<>(stockPriceService.getPrices(ticker), HttpStatus.OK);
    }

    @GetMapping("/stock/{ticker}/info")
    public ResponseEntity<StockInfoResponse> getStockInfoByTicker(
            @PathVariable String ticker) {
        StockInfoResponse stockInfoResponse = stockPriceService.findStockInfoByTicker(ticker);
        log.info(String.format("[GET] /stock/%s/info", ticker));
        return new ResponseEntity<>(stockInfoResponse, HttpStatus.OK);
    }

    @GetMapping("/stock/{ticker}/financial-info")
    public ResponseEntity<StockFinancialInfoResponse> getStockFinancialInfoByTicker(
            @PathVariable String ticker) {
        StockFinancialInfoResponse stockFinancialInfoResponse = stockPriceService.findStockFinancialInfoByTicker(
                ticker);
        log.info(String.format("[GET] /stock/%s/financial-info", ticker));
        return new ResponseEntity<>(stockFinancialInfoResponse, HttpStatus.OK);
    }
}
