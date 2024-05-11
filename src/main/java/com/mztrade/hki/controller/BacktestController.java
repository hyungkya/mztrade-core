package com.mztrade.hki.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.mztrade.hki.Util;
import com.mztrade.hki.dto.BacktestParameter;
import com.mztrade.hki.dto.BacktestResultResponse;
import com.mztrade.hki.dto.OrderResponse;
import com.mztrade.hki.dto.PositionResponse;
import com.mztrade.hki.dto.StockFinancialInfoResponse;
import com.mztrade.hki.dto.StockInfoResponse;
import com.mztrade.hki.dto.StockPriceResponse;
import com.mztrade.hki.dto.TagResponse;
import com.mztrade.hki.entity.Account;
import com.mztrade.hki.entity.ChartSetting;
import com.mztrade.hki.entity.CompareTableResponse;
import com.mztrade.hki.entity.TagRequest;
import com.mztrade.hki.entity.User;
import com.mztrade.hki.entity.backtest.BacktestResult;
import com.mztrade.hki.entity.backtest.Indicator;
import com.mztrade.hki.repository.AccountRepository;
import com.mztrade.hki.repository.UserRepository;
import com.mztrade.hki.service.AccountService;
import com.mztrade.hki.service.BacktestService;
import com.mztrade.hki.service.ChartSettingService;
import com.mztrade.hki.service.IndicatorService;
import com.mztrade.hki.service.OrderService;
import com.mztrade.hki.service.StatisticService;
import com.mztrade.hki.service.StockPriceService;
import com.mztrade.hki.service.TagService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class BacktestController {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private BacktestService backtestService;

    private StockPriceService stockPriceService;
    private OrderService orderService;
    private AccountService accountService;
    private StatisticService statisticService;
    private TagService tagService;
    private ChartSettingService chartSettingService;
    private IndicatorService indicatorService;

    private FirebaseAuth firebaseAuth;
    private ObjectMapper objectMapper;

    @Autowired
    public BacktestController(BacktestService backtestService, StockPriceService stockPriceService,
            OrderService orderService, AccountService accountService,
            StatisticService statisticService, TagService tagService,
            ChartSettingService chartSettingService, IndicatorService indicatorService,
            ObjectMapper objectMapper, AccountRepository accountRepository,
            UserRepository userRepository, FirebaseAuth firebaseAuth) {
        this.backtestService = backtestService;
        this.stockPriceService = stockPriceService;
        this.orderService = orderService;
        this.accountService = accountService;
        this.statisticService = statisticService;
        this.tagService = tagService;
        this.chartSettingService = chartSettingService;
        this.indicatorService = indicatorService;
        this.objectMapper = objectMapper;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.firebaseAuth = firebaseAuth;
    }

    @PostMapping("/backtest")
    public ResponseEntity<Boolean> createBacktest(@RequestBody BacktestParameter backtestParameter)
            throws JsonProcessingException {
        log.info(String.format("[POST] /backtest backtestParameter=%s", backtestParameter));

        int aid = backtestService.execute(backtestParameter);
        Account account = accountRepository.getReferenceById(aid);
        User user = userRepository.getReferenceById(backtestParameter.getUid());
        backtestService.create(BacktestResult.builder().account(account).user(user)
                .param(objectMapper.writeValueAsString(backtestParameter)).plratio(
                        backtestService.calculateFinalProfitLossRatio(
                                backtestParameter.getInitialBalance(), aid,
                                backtestParameter.parseEndDate())).build());

        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @GetMapping("/backtest/{aid}")
    public ResponseEntity<BacktestResultResponse> getBacktestResult(@PathVariable Integer aid) {
        BacktestResultResponse response = backtestService.get(aid);

        log.info(String.format("[GET] /backtest/aid=%s", aid));

        if (response == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/backtest/{aid}/param")
    public ResponseEntity<BacktestParameter> getBacktestParameter(@PathVariable Integer aid) {
        BacktestParameter backtestParameter = backtestService.getBacktestParameter(aid);

        log.info(String.format("[GET] /backtest/%s/param", aid));

        if (backtestParameter == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(backtestParameter, HttpStatus.OK);
    }

    @GetMapping("/backtest")
    public ResponseEntity<List<BacktestResultResponse>> getBacktestResults(
            @RequestParam Optional<Integer> uid,
            @RequestParam Optional<Integer> limit,
            @RequestParam(defaultValue = "") String title,
            @RequestParam Optional<List<Integer>> tids) {
        HttpStatus httpStatus = HttpStatus.OK;
        List<BacktestResultResponse> backtestResultResponses;
        if (uid.isPresent()) {
            if (tids.isPresent()) {
                backtestResultResponses = backtestService.searchBacktestResultByTags(uid.get(),
                        title, tids.get());
            } else {
                backtestResultResponses = backtestService.searchByTitle(uid.get(), title);
            }
        } else {
            backtestResultResponses = backtestService.getAllByPlratioDesc();
        }
        if (limit.isPresent()) {
            backtestResultResponses = backtestResultResponses.stream()
                    .sorted(Comparator.comparing(BacktestResultResponse::getPlratio).reversed())
                    .limit(limit.get()).toList();
        }
        return new ResponseEntity<>(backtestResultResponses, httpStatus);
    }

    @PutMapping("/backtest")
    public ResponseEntity<Boolean> updateBacktestResult(@RequestParam Integer aid,
                                                        @RequestBody BacktestParameter backtestParameter) throws JsonProcessingException {
        log.info(String.format("[PUT] /backtest/aid=%s", aid));
        backtestService.update(aid, objectMapper.writeValueAsString(backtestParameter));
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @DeleteMapping("/backtest")
    public ResponseEntity<Boolean> deleteBacktestResult(@RequestParam Integer aid) {
        log.info(String.format("[DELETE] /backtest/aid=%s", aid));
        accountService.deleteAccount(aid);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @GetMapping("/compare-chart/bt-table")
    public ResponseEntity<List<CompareTableResponse>> getCompareBtTable(
            @RequestParam List<Integer> aids) {
        List<CompareTableResponse> compareTableResponse = new ArrayList<>();

        for (Integer aid : aids) {
            compareTableResponse.add(CompareTableResponse.builder().aid(aid).ticker("")
                    .title(backtestService.getBacktestParameter(aid).getTitle()).subTitle("")
                    .plratio(backtestService.get(aid).getPlratio())
                    .winRate(statisticService.getTradingWinRate(aid))
                    .frequency(statisticService.getTradeFrequency(aid)).build());
        }
        log.info(String.format("[GET] /compare-chart/bt-table/aids=%s -> btTableList: %s", aids,
                compareTableResponse));

        return new ResponseEntity<>(compareTableResponse, HttpStatus.OK);
    }

    @GetMapping("/compare-chart/ticker-table")
    public ResponseEntity<List<CompareTableResponse>> getCompareTickerTable(
            @RequestParam List<Integer> aids) {
        List<CompareTableResponse> compareTableResponse = new ArrayList<>();

        for (Integer aid : aids) {
            List<String> tickers = backtestService.getBacktestParameter(aid).getTickers();
            for (String ticker : tickers) {
                compareTableResponse.add(CompareTableResponse.builder().aid(aid).ticker(ticker)
                        .title(backtestService.getBacktestParameter(aid).getTitle())
                        .subTitle(stockPriceService.findStockInfoByTicker(ticker).getName())
                        .plratio(statisticService.getTickerProfit(aid, ticker))
                        .winRate(statisticService.getTickerTradingWinRate(aid, ticker))
                        .frequency(statisticService.getTickerTradeFrequency(aid, ticker)).build());
            }
        }
        log.info(String.format("[GET] /compare-chart/ticker-table/aids=%s -> tickerTableList: %s",
                aids, compareTableResponse));

        return new ResponseEntity<>(compareTableResponse, HttpStatus.OK);
    }

    @GetMapping("/stock_info/tag")
    public ResponseEntity<List<TagResponse>> getStockInfoTag(@RequestParam Integer uid) {
        List<TagResponse> tagResponses = tagService.getStockInfoTag(uid);
        log.info(String.format("[GET] /stock_info/tag/uid=%s", uid));
        return new ResponseEntity<>(tagResponses, HttpStatus.OK);
    }

    @GetMapping("/stock_info/tag-by-ticker")
    public ResponseEntity<List<TagResponse>> getStockInfoTagByTicker(@RequestParam Integer uid,
            @RequestParam String ticker) {
        List<TagResponse> tagResponses = tagService.getStockInfoTagByTicker(uid, ticker);
        log.info(String.format("[GET] /stock_info/tag/uid=%s&ticker=%s", uid, ticker));
        return new ResponseEntity<>(tagResponses, HttpStatus.OK);
    }

    @PostMapping("/stock_info/tag-link")
    public ResponseEntity<Boolean> createStockInfoTagLink(@RequestParam Integer tid,
            @RequestParam String ticker) {
        Boolean isProcessed = tagService.createStockInfoTagLink(tid, ticker);
        log.info(String.format("[POST] /stock_info/tag-link/tid=%s&ticker=%s", tid, ticker));
        return new ResponseEntity<>(isProcessed, HttpStatus.OK);
    }

    @DeleteMapping("/stock_info/tag-link")
    public ResponseEntity<Boolean> deleteStockInfoTagLink(@RequestParam Integer tid,
            @RequestParam String ticker) {
        tagService.deleteStockInfoTagLink(tid, ticker);
        log.info(String.format("[DELETE] /stock_info/tag-link/tid=%s&ticker=%s", tid, ticker));
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @GetMapping("/backtest/tag")
    public List<TagResponse> getBacktestResultTag(@RequestParam Integer uid) {
        List<TagResponse> tagResponses = tagService.getBacktestResultTag(uid);
        log.info(String.format("[GET] /backtest/tag/uid=%s", uid));
        return tagResponses;
    }

    @GetMapping("/backtest/tag-by-aid")
    public List<TagResponse> getBacktestResultTag(@RequestParam Integer uid,
            @RequestParam Integer aid) {
        List<TagResponse> tagResponses = tagService.getBacktestResultTagByAid(uid, aid);
        log.info(String.format("[GET] /backtest/tag/uid=%s&aid=%s", uid, aid));
        return tagResponses;
    }

    @PostMapping("/backtest/tag-link")
    public ResponseEntity<Boolean> createBacktestResultTagLink(@RequestParam Integer tid,
            @RequestParam Integer aid) {
        Boolean isProcessed = tagService.createBacktestResultTagLink(tid, aid);
        log.info(String.format("[POST] /backtest/tag-link/tid=%s&aid=%s", tid, aid));
        return new ResponseEntity<>(isProcessed, HttpStatus.OK);
    }

    @DeleteMapping("/backtest/tag-link")
    public ResponseEntity<Boolean> deleteBacktestResultTagLink(@RequestParam Integer tid,
            @RequestParam Integer aid) {
        tagService.deleteBacktestResultTagLink(tid, aid);
        log.info(String.format("[DELETE] /backtest/tag-link/tid=%s&aid=%s", tid, aid));
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @PostMapping("/tag")
    public boolean createTag(@RequestBody TagRequest tagRequest) {
        int create = tagService.createTag(tagRequest.getUid(), tagRequest.getName(),
                tagRequest.getColor(), tagRequest.getCategory());
        log.info(String.format("[POST] /tag tag=%s", tagRequest));
        return create > 0;
    }

    @DeleteMapping("/tag")
    public ResponseEntity<Boolean> deleteTag(@RequestParam Integer tid) {
        tagService.deleteTag(tid);
        log.info(String.format("[DELETE] /tag/tid=%s", tid));
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @PutMapping("/tag")
    public boolean updateTag(@RequestParam Integer tid, @RequestParam String name,
            @RequestParam String color) {
        boolean update = tagService.updateTag(tid, name, color);
        log.info(String.format("[PUT] /tag/tid=%s&name=%s&color=%s", tid, name, color));
        return update;
    }

    @GetMapping("/chart-setting")
    public ChartSetting getChartSetting(@RequestParam int uid) {
        ChartSetting chartSetting = chartSettingService.get(uid);
        log.info(String.format("[GET] /chart-setting?uid=%s", chartSetting));
        return chartSetting;
    }

    @PutMapping("/chart-setting")
    public boolean saveChartSetting(@RequestParam int uid, @RequestParam String indicator) {
        ChartSetting chartSetting = ChartSetting.builder().uid(uid).indicator(indicator).build();
        boolean update = chartSettingService.save(chartSetting);
        log.info(String.format("[PUT] /chart-setting?uid=%s&indicator=%s -> update:%b", uid,
                indicator, update));
        return update;
    }

    @GetMapping("/positions")
    public ResponseEntity<List<PositionResponse>> getPositions(@RequestParam Integer aid) {
        log.info(String.format("[GET] /positions?aid=%s", aid));
        return new ResponseEntity<>(orderService.getPositions(aid), HttpStatus.OK);
    }

    @GetMapping("/cash-balance")
    public ResponseEntity<Long> getBalance(@RequestParam Integer aid) {
        log.info(String.format("[GET] /balance?aid=%s", aid));
        return new ResponseEntity<>(accountService.getBalance(aid), HttpStatus.OK);
    }

    @GetMapping("/order_history")
    public ResponseEntity<List<OrderResponse>> getOrderHistory(@RequestParam Integer aid) {
        log.info(String.format("[GET] /order_history?aid=%s", aid));
        return new ResponseEntity<>(orderService.getOrderHistory(aid), HttpStatus.OK);
    }

    @GetMapping("/stock_price")
    public ResponseEntity<List<StockPriceResponse>> getPricesByTicker(@RequestParam String ticker) {
        log.info(String.format("[GET] /stock_price/ticker=%s", ticker));
        return new ResponseEntity<>(stockPriceService.getPrices(ticker), HttpStatus.OK);
    }

    @GetMapping("/stock_info")
    public ResponseEntity<StockInfoResponse> getStockInfoByTicker(@RequestParam String ticker) {
        StockInfoResponse stockInfoResponse = stockPriceService.findStockInfoByTicker(ticker);
        log.info(String.format("[GET] /stock_info?ticker=%s", ticker));
        return new ResponseEntity<>(stockInfoResponse, HttpStatus.OK);
    }

    @GetMapping("/stock_financial_info")
    public ResponseEntity<StockFinancialInfoResponse> getStockFinancialInfoByTicker(
            @RequestParam String ticker) {
        StockFinancialInfoResponse stockFinancialInfoResponse = stockPriceService.findStockFinancialInfoByTicker(
                ticker);
        log.info(String.format("[GET] /stock_financial_info?ticker=%s", ticker));
        return new ResponseEntity<>(stockFinancialInfoResponse, HttpStatus.OK);
    }

    @GetMapping("/stock")
    public ResponseEntity<List<StockInfoResponse>> getAllStockInfo() {
        log.info("[GET] /stock");
        return new ResponseEntity<>(stockPriceService.getAllStockInfo(), HttpStatus.OK);
    }

    @GetMapping("/stock/search")
    public ResponseEntity<List<StockInfoResponse>> searchStockInfoByName(
            @RequestParam String name) {
        log.info("[GET] /stock/search?name=%s", name);
        return new ResponseEntity<>(stockPriceService.searchStockInfoByName(name), HttpStatus.OK);
    }

    @GetMapping("/stock/search-tags")
    public ResponseEntity<List<StockInfoResponse>> searchStockInfoByNameAndTags(
            @RequestParam int uid, @RequestParam String name, @RequestParam List<Integer> tids) {
        log.info("[GET] /stock/search?uid=%d&name=%s&tids=%s", uid, name, tids);
        return new ResponseEntity<>(tagService.findStockInfoByNameAndTags(uid, name, tids),
                HttpStatus.OK);
    }

    @GetMapping("/stock_price/indicator")
    public ResponseEntity<Double> getSingleIndicatorByTicker(@RequestParam String ticker,
            @RequestParam String date, @RequestParam String type, @RequestParam List<Float> param) {
        log.info(String.format("[GET] /stock_price/indicator/ticker=%s&date=%s&type=%s&param=%s",
                ticker, date, type, param));

        return new ResponseEntity<>(
                indicatorService.getIndicator(ticker, Util.stringToLocalDateTime(date), type,
                        param), HttpStatus.OK);
    }

    @GetMapping("/stock_price/indicators")
    public ResponseEntity<Map<LocalDateTime, Double>> getIndicatorByTicker(
            @RequestParam String ticker, @RequestParam String startDate,
            @RequestParam String endDate, @RequestParam String type,
            @RequestParam List<Float> param) {
        log.info(String.format(
                "[GET] /stock_price/indicators/ticker=%s&startDate=%s&endDate=%s&type=%s&param=%s",
                ticker, startDate, endDate, type, param));

        return new ResponseEntity<>(
                indicatorService.getIndicators(ticker, Util.stringToLocalDateTime(startDate),
                        Util.stringToLocalDateTime(endDate),
                        Indicator.builder().type(type).params(param).build()), HttpStatus.OK);
    }

    @GetMapping("/statistic/getWinRate")
    public ResponseEntity<Double> getTradingWinRate(@RequestParam Integer aid) {
        Double winRate = statisticService.getTradingWinRate(aid);

        log.info(String.format("[GET] /statistic/getWinRate/aid=%s", aid));

        return new ResponseEntity<Double>(winRate, HttpStatus.OK);
    }

    @GetMapping("/statistic/tradeFrequency")
    public ResponseEntity<Double> getTradeFrequency(@RequestParam Integer aid) {
        Double tradeFrequency = statisticService.getTradeFrequency(aid);
        log.info(String.format("[GET] /statistic/tradeFrequency/aid=%s", aid));
        return new ResponseEntity<Double>(tradeFrequency, HttpStatus.OK);
    }

    @GetMapping("/statistic/ticker-profit")
    public ResponseEntity<Double> getTickerProfit(@RequestParam Integer aid,
            @RequestParam String ticker) {
        log.info(String.format("[GET] /statistic/ticker-profit/aid=%s&ticker=%s", aid, ticker));
        return new ResponseEntity<>(statisticService.getTickerProfit(aid, ticker), HttpStatus.OK);
    }

    @GetMapping("/statistic/ticker-profit/all")
    public ResponseEntity<Map<String, Double>> getTickerProfit(@RequestParam Integer aid) {
        log.info(String.format("[GET] /statistic/ticker-profit/all/aid=%s", aid));
        return new ResponseEntity<>(statisticService.getTickerProfit(aid), HttpStatus.OK);
    }

    @GetMapping("/statistic/ticker-trade-count")
    public ResponseEntity<Integer> getTickerTradeCount(@RequestParam Integer aid,
            @RequestParam String ticker, @RequestParam(defaultValue = "0") Integer option) {
        log.info(
                String.format("[GET] /statistic/ticker-trade-count/aid=%s&ticker=%s&option=%s", aid,
                        ticker, option));
        return new ResponseEntity<>(statisticService.getTickerTradeCount(aid, ticker, option),
                HttpStatus.OK);
    }

    @GetMapping("/statistic/ticker-benchmark-profit/all")
    public ResponseEntity<Map<String, Double>> getTickerBenchmarkProfit(@RequestParam Integer aid) {
        log.info(String.format("[GET] /statistic/ticker-benchmark-profit/all/aid=%s", aid));
        return new ResponseEntity<>(statisticService.getTickerBenchmarkProfit(aid), HttpStatus.OK);
    }

    @GetMapping("/statistic/ticker-alpha-profit/all")
    public ResponseEntity<Map<String, Double>> getTickerAlphaProfit(@RequestParam Integer aid) {
        log.info(String.format("[GET] /statistic/ticker=alpha-profit/all/aid=%s", aid));
        return new ResponseEntity<>(statisticService.getTickerAlphaProfit(aid), HttpStatus.OK);
    }


}
