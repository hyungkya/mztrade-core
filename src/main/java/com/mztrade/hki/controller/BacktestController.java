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
import com.mztrade.hki.service.*;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@Slf4j
public class BacktestController {

    private BacktestService backtestService;
    private StockPriceService stockPriceService;
    private OrderService orderService;
    private AccountService accountService;

    private StatisticService statisticService;
    private ObjectMapper objectMapper;

    @Autowired
    public BacktestController(BacktestService backtestService,
                              StockPriceService stockPriceService,
                              OrderService orderService,
                              AccountService accountService,
                              StatisticService statisticService,
                              ObjectMapper objectMapper)
    {
        this.backtestService = backtestService;
        this.stockPriceService = stockPriceService;
        this.orderService = orderService;
        this.accountService = accountService;
        this.statisticService = statisticService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/execute")
    public ResponseEntity<Boolean> backtest(
            @RequestBody BacktestRequest backtestRequest
            ) throws JsonProcessingException {
        int uid = backtestRequest.parseUid();
        String title = backtestRequest.getTitle();
        long initialBalance = backtestRequest.parseInitialBalance();
        List<List<Condition>> buyConditions = backtestRequest.parseBuyConditions();
        List<List<Condition>> sellConditions = backtestRequest.parseSellConditions();
        List<Float> dca = backtestRequest.getDca();
        int maxTrading = backtestRequest.parseMaxTrading();
        List<String> tickers = backtestRequest.getTickers();
        LocalDateTime startDate = backtestRequest.parseStartDate();
        LocalDateTime endDate = backtestRequest.parseEndDate();

        log.info(String.format("[POST] /excute backtestRequest=%s",backtestRequest));

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

        backtestService.create(BacktestHistory.builder()
                .aid(aid)
                .uid(uid)
                .param(objectMapper.writeValueAsString(backtestRequest))
                .plratio(backtestService.calculateFinalProfitLossRatio(initialBalance, aid, endDate))
                .build()
        );

        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @GetMapping("/backtest/{aid}")
    public ResponseEntity<BacktestHistory> getBacktestHistory(
            @PathVariable Integer aid
    ) {
        BacktestHistory backtestHistory = backtestService.get(aid);

        log.info(String.format("[GET] /backtest/aid=%s",aid));

        if (backtestHistory == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(backtestHistory, HttpStatus.OK);
    }

    @GetMapping("/backtest/param/{aid}")
    public ResponseEntity<BacktestRequest> getBacktestHistoryParameter(
            @PathVariable Integer aid
    ) {
        BacktestRequest backtestRequest = backtestService.getBacktestRequest(aid);

        log.info(String.format("[GET] /backtest/param/aid=%s",aid));

        if (backtestRequest == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(backtestRequest, HttpStatus.OK);
    }

    @GetMapping("/backtest/count/{uid}")
    public ResponseEntity<Integer> getUserBacktestHistoryCount(
            @PathVariable Integer uid
    ) {
        Integer recordCount = backtestService.getNumberOfHistoryByUid(uid);

        log.info(String.format("[GET] /backtest/count/uid=%s",uid));

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

        log.info(String.format("[GET] /backtest/all/uid=%s",uid));

        return new ResponseEntity<>(backtestHistories, HttpStatus.OK);
    }

    @GetMapping("/backtest/ranking")
    public ResponseEntity<List<BacktestHistory>> getBacktestRanking() {
        List<BacktestHistory> backtestHistories = backtestService.getRanking();
        return new ResponseEntity<>(backtestHistories, HttpStatus.OK);
    }

    @GetMapping("/backtest/search")
    public ResponseEntity<List<BacktestHistory>> searchBacktestHistory(
            @RequestParam Integer uid,
            @RequestParam String title
    ) {
        List<BacktestHistory> queryResult = backtestService.searchByTitle(uid, title);

        log.info(String.format("[GET] /backtest/search/uid=%s&title=%s",uid,title));

        return new ResponseEntity<>(queryResult, HttpStatus.OK);
    }

    @DeleteMapping("/backtest")
    public ResponseEntity<Boolean> deleteAccount(
            @RequestParam Integer aid
    ) {
        log.info(String.format("[DELETE] /backtest/aid=%s",aid));
        accountService.deleteAccount(aid);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @GetMapping("/backtest/top-plratio")
    public ResponseEntity<BacktestHistory> getHighestProfitLossRatio(
            @RequestParam Integer uid
    ) {
        Optional<Integer> highestProfitLossRatioAid = backtestService.getHighestProfitLossRatio(uid);

        log.info(String.format("[GET] /backtest/top-plratio/uid=%s",uid));

        return highestProfitLossRatioAid.map(
                        aid -> new ResponseEntity<>(backtestService.get(aid), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.OK));
    }

    @GetMapping("/order_history")
    public ResponseEntity<List<Order>> getOrderHistory(
            @RequestParam Integer aid
    ) {
        log.info(String.format("[GET] /order_history/aid=%s",aid));
        return new ResponseEntity<>(orderService.getOrderHistory(aid), HttpStatus.OK);
    }

    @GetMapping("/stock_price")
    public ResponseEntity<List<Bar>> getPricesByTicker(
            @RequestParam String ticker
    ) {
        log.info(String.format("[GET] /stock+price/ticker=%s",ticker));
        return new ResponseEntity<>(stockPriceService.getPrices(ticker), HttpStatus.OK);
    }

    @GetMapping("/stock")
    public ResponseEntity<List<StockInfo>> getAllStockInfo() {
        return new ResponseEntity<>(stockPriceService.getAllStockInfo(), HttpStatus.OK);
    }

    @GetMapping("/stock_price/indicator")
    public ResponseEntity<Map<LocalDateTime, Double>> getIndicatorByTicker(
            @RequestParam String ticker,
            @RequestParam String type,
            @RequestParam String param
    ) {
        List<Float> parsedParam = Stream.of(param.split(","))
                .map(p -> Float.parseFloat(p.trim())).collect(Collectors.toList());

        log.info(String.format("[GET] /stock_price/indicator/ticker=%s&type=%s&param=%s",ticker,type,param));

        return new ResponseEntity<>(
                stockPriceService.getIndicator(ticker, type, parsedParam),
                HttpStatus.OK
        );
    }

    @GetMapping("/statistic/getWinRate")
    public ResponseEntity<Double> getTradingWinRate(
            @RequestParam Integer aid
    ) {
        Double winRate = statisticService.getTradingWinRate(aid);

        log.info(String.format("[GET] /statistic/getWinRate/aid=%s",aid));

        return new ResponseEntity<Double>(winRate, HttpStatus.OK);
    }

    @GetMapping("/statistic/tradeFrequency")
    public ResponseEntity<Double> getTradeFrequency(
            @RequestParam Integer aid
    ) {
        Double tradeFrequency = statisticService.getTradeFrequency(aid);
        log.info(String.format("[GET] /statistic/tradeFrequency/aid=%s",aid));
        return new ResponseEntity<Double>(tradeFrequency, HttpStatus.OK);
    }

    @GetMapping("/statistic/ticker-profit")
    public ResponseEntity<Double> getTickerProfit(
            @RequestParam Integer aid,
            @RequestParam String ticker
    ) {
        log.info(String.format("[GET] /statistic/ticker-profit/aid=%s&ticker=%s",aid,ticker));
        return new ResponseEntity<>(statisticService.getTickerProfit(aid, ticker), HttpStatus.OK);
    }

    @GetMapping("/statistic/ticker-profit/all")
    public ResponseEntity<Map<String, Double>> getTickerProfit(
            @RequestParam Integer aid
    ) {
        log.info(String.format("[GET] /statistic/ticker-profit/all/aid=%s",aid));
        return new ResponseEntity<>(statisticService.getTickerProfit(aid), HttpStatus.OK);
    }

    @GetMapping("/statistic/ticker-trade-count")
    public ResponseEntity<Integer> getTickerTradeCount(
            @RequestParam Integer aid,
            @RequestParam String ticker,
            @RequestParam(defaultValue = "0") Integer option
    ) {
        log.info(String.format("[GET] /statistic/ticker-trade-count/aid=%s&ticker=%s&option=%s",aid,ticker,option));
        return new ResponseEntity<>(statisticService.getTickerTradeCount(aid, ticker, option), HttpStatus.OK);
    }

    @GetMapping("/statistic/ticker-benchmark-profit/all")
    public ResponseEntity<Map<String, Double>> getTickerBenchmarkProfit(
            @RequestParam Integer aid
    ) {
        log.info(String.format("[GET] /statistic/ticker-benchmark-profit/all/aid=%s",aid));
        return new ResponseEntity<>(statisticService.getTickerBenchmarkProfit(aid), HttpStatus.OK);
    }

    @GetMapping("/statistic/ticker-alpha-profit/all")
    public ResponseEntity<Map<String, Double>> getTickerAlphaProfit(
            @RequestParam Integer aid
    ) {
        log.info(String.format("[GET] /statistic/ticker=alpha-profit/all/aid=%s",aid));
        return new ResponseEntity<>(statisticService.getTickerAlphaProfit(aid), HttpStatus.OK);
    }
}
