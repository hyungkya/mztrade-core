package com.mztrade.hki.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.Util;
import com.mztrade.hki.entity.*;
import com.mztrade.hki.entity.backtest.BacktestHistory;
import com.mztrade.hki.entity.backtest.BacktestRequest;
import com.mztrade.hki.entity.backtest.Condition;
import com.mztrade.hki.entity.backtest.IndicatorBar;
import com.mztrade.hki.service.*;

import java.text.DecimalFormat;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    private TagService tagService;
    private ChartSettingService chartSettingService;
    private IndicatorService indicatorService;
    private ObjectMapper objectMapper;

    @Autowired
    public BacktestController(BacktestService backtestService,
                              StockPriceService stockPriceService,
                              OrderService orderService,
                              AccountService accountService,
                              StatisticService statisticService,
                              TagService tagService,
                              ChartSettingService chartSettingService,
                              IndicatorService indicatorService,
                              ObjectMapper objectMapper)
    {
        this.backtestService = backtestService;
        this.stockPriceService = stockPriceService;
        this.orderService = orderService;
        this.accountService = accountService;
        this.statisticService = statisticService;
        this.tagService = tagService;
        this.chartSettingService = chartSettingService;
        this.indicatorService = indicatorService;
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

    @GetMapping("/backtest/search-by-tags")
    public ResponseEntity<List<BacktestHistory>> searchBacktestHistoryWithTags(
            @RequestParam Integer uid,
            @RequestParam String title,
            @RequestParam List<Integer> tids
    ) {
        List<BacktestHistory> queryResult = backtestService.searchBacktestHistoryByTags(uid, title, tids);

        log.info(String.format("[GET] /backtest/search-by-tags/uid=%s&title=%s&tids=%s",uid,title,tids));

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

    @GetMapping("/compare-chart/bt-table")
    public ResponseEntity<List<CompareTableResponse>> getCompareBtTable(
            @RequestParam List<Integer> aids
    ) {
        List<CompareTableResponse> compareTableResponse = new ArrayList<>();

        for(Integer aid : aids) {
            compareTableResponse.add(CompareTableResponse.builder()
                    .aid(aid)
                    .title(backtestService.getBacktestRequest(aid).getTitle())
                    .subTitle("")
                    .plratio(backtestService.get(aid).getPlratio())
                    .winRate(statisticService.getTradingWinRate(aid))
                    .frequency(statisticService.getTradeFrequency(aid)
                    ).build());
        }
        log.info(String.format("[GET] /compare-chart/bt-table/aids=%s -> btTableList: %s",aids, compareTableResponse));

        return new ResponseEntity<>(compareTableResponse, HttpStatus.OK);
    }

    @GetMapping("/compare-chart/ticker-table")
    public ResponseEntity<List<CompareTableResponse>> getCompareTickerTable(
            @RequestParam List<Integer> aids
    ) {
        List<CompareTableResponse> compareTableResponse = new ArrayList<>();

        for(Integer aid : aids) {
            List<String> tickers = backtestService.getBacktestRequest(aid).getTickers();
            for(String ticker : tickers){
                compareTableResponse.add(CompareTableResponse.builder()
                    .aid(aid)
                    .title(backtestService.getBacktestRequest(aid).getTitle())
                    .subTitle(stockPriceService.findStockInfoByTicker(ticker).getName())
                    .plratio(statisticService.getTickerProfit(aid,ticker))
                    .winRate(statisticService.getTickerTradingWinRate(aid,ticker))
                    .frequency(statisticService.getTickerTradeFrequency(aid,ticker))
                    .build());
            }
        }
        log.info(String.format("[GET] /compare-chart/ticker-table/aids=%s -> tickerTableList: %s",aids, compareTableResponse));

        return new ResponseEntity<>(compareTableResponse, HttpStatus.OK);
    }

    @GetMapping("/stock_info/tag")
    public ResponseEntity<List<Tag>> getStockInfoTag(
            @RequestParam Integer uid
    ) {
        List<Tag> tags = tagService.getStockInfoTag(uid);
        log.info(String.format("[GET] /stock_info/tag/uid=%s",uid));
        return new ResponseEntity<>(tags, HttpStatus.OK);
    }

    @GetMapping("/stock_info/tag-by-ticker")
    public ResponseEntity<List<Tag>> getStockInfoTagByTicker(
            @RequestParam Integer uid,
            @RequestParam String ticker
    ) {
        List<Tag> tags = tagService.getStockInfoTagByTicker(uid, ticker);
        log.info(String.format("[GET] /stock_info/tag/uid=%s&ticker=%s", uid, ticker));
        return new ResponseEntity<>(tags, HttpStatus.OK);
    }

    @PostMapping("/stock_info/tag-link")
    public ResponseEntity<Boolean> createStockInfoTagLink(
            @RequestParam Integer tid,
            @RequestParam String ticker
    ) {
        Boolean isProcessed = tagService.createStockInfoTagLink(tid, ticker);
        log.info(String.format("[POST] /stock_info/tag-link/tid=%s&ticker=%s",tid,ticker));
        return new ResponseEntity<>(isProcessed, HttpStatus.OK);
    }

    @DeleteMapping("/stock_info/tag-link")
    public ResponseEntity<Boolean> deleteStockInfoTagLink(
            @RequestParam Integer tid,
            @RequestParam String ticker
    ) {
        Boolean isProcessed = tagService.deleteStockInfoTagLink(tid, ticker);
        log.info(String.format("[DELETE] /stock_info/tag-link/tid=%s&ticker=%s",tid,ticker));
        return new ResponseEntity<>(isProcessed, HttpStatus.OK);
    }

    @GetMapping("/backtest/tag")
    public List<Tag> getBacktestHistoryTag(
            @RequestParam Integer uid
    ) {
        List<Tag> tags = tagService.getBacktestHistoryTag(uid);
        log.info(String.format("[GET] /backtest/tag/uid=%s",uid));
        return tags;
    }

    @GetMapping("/backtest/tag-by-aid")
    public List<Tag> getBacktestHistoryTag(
            @RequestParam Integer uid,
            @RequestParam Integer aid
    ) {
        List<Tag> tags = tagService.getBacktestHistoryTagByAid(uid,aid);
        log.info(String.format("[GET] /backtest/tag/uid=%s&aid=%s",uid,aid));
        return tags;
    }

    @PostMapping("/backtest/tag-link")
    public ResponseEntity<Boolean> createBacktestHistoryTagLink(
        @RequestParam Integer tid,
        @RequestParam Integer aid
    ) {
        Boolean isProcessed = tagService.createBacktestHistoryTagLink(tid, aid);
        log.info(String.format("[POST] /backtest/tag-link/tid=%s&aid=%s",tid,aid));
        return new ResponseEntity<>(isProcessed, HttpStatus.OK);
    }

    @DeleteMapping("/backtest/tag-link")
    public ResponseEntity<Boolean> deleteBacktestHistoryTagLink(
            @RequestParam Integer tid,
            @RequestParam Integer aid
    ) {
        Boolean isProcessed = tagService.deleteBacktestHistoryTagLink(tid, aid);
        log.info(String.format("[DELETE] /backtest/tag-link/tid=%s&aid=%s",tid,aid));
        return new ResponseEntity<>(isProcessed, HttpStatus.OK);
    }

    @PostMapping("/tag")
    public boolean createTag(
            @RequestBody TagRequest tagRequest
    ) {
        int create = tagService.createTag(tagRequest.getUid(),tagRequest.getName(),tagRequest.getColor(),tagRequest.getCategory());
        log.info(String.format("[POST] /tag tag=%s",tagRequest));
        return create > 0;
    }

    @DeleteMapping("/tag")
    public boolean deleteTag(
            @RequestParam Integer tid
    ) {
        boolean delete = tagService.deleteTag(tid);
        log.info(String.format("[DELETE] /tag/tid=%s",tid));
        return delete;
    }

    @PutMapping("/tag")
    public boolean updateTag(
            @RequestParam Integer tid,
            @RequestParam String name,
            @RequestParam String color
    ) {
        boolean update = tagService.updateTag(tid,name,color);
        log.info(String.format("[PUT] /tag/tid=%s&name=%s&color=%s",tid,name,color));
        return update;
    }

    @GetMapping("/chart-setting")
    public ChartSetting getChartSetting(
            @RequestParam int uid
    ) {
        ChartSetting chartSetting = chartSettingService.get(uid);
        log.info(String.format("[GET] /chart-setting?uid=%s",chartSetting));
        return chartSetting;
    }

    @PutMapping("/chart-setting")
    public boolean saveChartSetting(
            @RequestParam int uid,
            @RequestParam String indicator
    ) {
        ChartSetting chartSetting = ChartSetting.builder().uid(uid).indicator(indicator).build();
        boolean update = chartSettingService.save(chartSetting);
        log.info(String.format("[PUT] /chart-setting?uid=%s&indicator=%s -> update:%b", uid, indicator, update));
        return update;
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
        log.info(String.format("[GET] /stock_price/ticker=%s",ticker));
        return new ResponseEntity<>(stockPriceService.getPrices(ticker), HttpStatus.OK);
    }

    @GetMapping("/stock")
    public ResponseEntity<List<StockInfo>> getAllStockInfo() {
        log.info("[GET] /stock");
        return new ResponseEntity<>(stockPriceService.getAllStockInfo(), HttpStatus.OK);
    }

    @GetMapping("/stock/search")
    public ResponseEntity<List<StockInfo>> searchStockInfoByName(
            @RequestParam String name
    ) {
        log.info("[GET] /stock/search?name=%s", name);
        return new ResponseEntity<>(stockPriceService.searchStockInfoByName(name), HttpStatus.OK);
    }

    @GetMapping("/stock/search-tags")
    public ResponseEntity<List<StockInfo>> searchStockInfoByNameAndTags(
            @RequestParam int uid,
            @RequestParam String name,
            @RequestParam List<Integer> tids
    ) {
        log.info("[GET] /stock/search?uid=%d&name=%s&tids=%s", uid, name, tids);
        return new ResponseEntity<>(tagService.findStockInfoByNameAndTags(uid, name, tids), HttpStatus.OK);
    }

    @GetMapping("/stock_price/indicator")
    public ResponseEntity<Map<LocalDateTime, Double>> getIndicatorByTicker(
            @RequestParam String ticker,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String type,
            @RequestParam List<Float> param
    ) {
        log.info(String.format("[GET] /stock_price/indicator/ticker=%s&startDate=%s&endDate=%s&type=%s&param=%s",ticker,startDate,endDate,type,param));

        return new ResponseEntity<>(
                indicatorService.getIndicator(ticker, Util.stringToLocalDateTime(startDate), Util.stringToLocalDateTime(endDate), type, param),
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
