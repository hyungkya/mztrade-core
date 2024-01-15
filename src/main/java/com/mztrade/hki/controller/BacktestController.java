package com.mztrade.hki.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.entity.Bar;
import com.mztrade.hki.entity.Order;
import com.mztrade.hki.entity.StockInfo;
import com.mztrade.hki.entity.backtest.BacktestHistory;
import com.mztrade.hki.entity.backtest.BacktestRequest;
import com.mztrade.hki.entity.backtest.Condition;
import com.mztrade.hki.entity.backtest.IndicatorBar;
import com.mztrade.hki.service.AccountService;
import com.mztrade.hki.service.BacktestService;
import com.mztrade.hki.service.OrderService;
import com.mztrade.hki.service.StockPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
public class BacktestController {

    private BacktestService backtestService;
    private StockPriceService stockPriceService;
    private OrderService orderService;
    private AccountService accountService;
    private ObjectMapper objectMapper;

    @Autowired
    public BacktestController(BacktestService backtestService,
                              StockPriceService stockPriceService,
                              OrderService orderService,
                              AccountService accountService,
                              ObjectMapper objectMapper)
    {
        this.backtestService = backtestService;
        this.stockPriceService = stockPriceService;
        this.orderService = orderService;
        this.accountService = accountService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/execute")
    public ResponseEntity<Boolean> backtest(
            @RequestBody BacktestRequest backtestRequest
            ) throws JsonProcessingException {
        int uid = backtestRequest.parseUid();
        long initialBalance = backtestRequest.parseInitialBalance();
        List<List<Condition>> buyConditions = backtestRequest.parseBuyConditions();
        List<List<Condition>> sellConditions = backtestRequest.parseSellConditions();
        List<Float> dca = backtestRequest.getDca();
        int maxTrading = backtestRequest.parseMaxTrading();
        List<String> tickers = backtestRequest.getTickers();
        Instant startDate = backtestRequest.parseStartDate();
        Instant endDate = backtestRequest.parseEndDate();

        int aid = backtestService.execute(
                uid,
                initialBalance,
                buyConditions,
                sellConditions,
                dca,
                maxTrading,
                tickers,
                startDate,
                endDate
        );

        backtestService.create(new BacktestHistory()
                .setAid(aid)
                .setUid(uid)
                .setParam(objectMapper.writeValueAsString(backtestRequest))
        );

        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @GetMapping("/backtest/{aid}")
    public ResponseEntity<String> getBacktestHistory(
            @PathVariable Integer aid
    ) {
        BacktestHistory backtestHistory = backtestService.get(aid);
        if (backtestHistory == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(backtestHistory.getParam(), HttpStatus.OK);
    }

    @GetMapping("/backtest/count/{uid}")
    public ResponseEntity<Integer> getUserBacktestHistoryCount(
            @PathVariable Integer uid
    ) {
        Integer recordCount = backtestService.getNumberOfHistoryByUid(uid);
        return new ResponseEntity<>(recordCount, HttpStatus.OK);
    }

    @GetMapping("/backtest/all")
    public ResponseEntity<List<BacktestHistory>> getAllBacktestHistory(
            @RequestParam Integer uid
    ) {
        List<BacktestHistory> backtestHistories = new ArrayList<>();
        for (Integer aid : accountService.getAll(uid)) {
            BacktestHistory queryResult = backtestService.get(aid);
            if (queryResult != null) {
                backtestHistories.add(queryResult);
            }
        }
        return new ResponseEntity<>(backtestHistories, HttpStatus.OK);
    }

    @GetMapping("/order_history")
    public ResponseEntity<List<Order>> getOrderHistory(
            @RequestParam Integer aid
    ) {
        return new ResponseEntity<>(orderService.getOrderHistory(aid), HttpStatus.OK);
    }

    @GetMapping("/stock_price")
    public ResponseEntity<List<Bar>> getPricesByTicker(
            @RequestParam String ticker
    ) {
        System.out.println("/stock_price/ticker?=" + ticker + " has been called.");
        return new ResponseEntity<>(stockPriceService.getPrices(ticker), HttpStatus.OK);
    }

    @GetMapping("/stock")
    public ResponseEntity<List<StockInfo>> getAllStockInfo() {
        return new ResponseEntity<>(stockPriceService.getAllStockInfo(), HttpStatus.OK);
    }

    @GetMapping("/stock_price/indicator")
    public ResponseEntity<Map<Instant, Double>> getIndicatorByTicker(
            @RequestParam String ticker,
            @RequestParam String type,
            @RequestParam String param
    ) {
        List<Float> parsedParam = Stream.of(param.split(","))
                .map(p -> Float.parseFloat(p.trim())).collect(Collectors.toList());
        return new ResponseEntity<>(
                stockPriceService.getIndicator(ticker, type, parsedParam),
                HttpStatus.OK
        );
    }
}
