package com.mztrade.hki.controller;

import com.mztrade.hki.entity.CompareTableResponse;
import com.mztrade.hki.service.AccountService;
import com.mztrade.hki.service.BacktestService;
import com.mztrade.hki.service.StatisticService;
import com.mztrade.hki.service.StockPriceService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class CompareChartController {

    private final BacktestService backtestService;
    private final StatisticService statisticService;
    private final AccountService accountService;
    private final StockPriceService stockPriceService;

    public CompareChartController(BacktestService backtestService,
            StatisticService statisticService, AccountService accountService,
            StockPriceService stockPriceService) {
        this.backtestService = backtestService;
        this.statisticService = statisticService;
        this.accountService = accountService;
        this.stockPriceService = stockPriceService;
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

    @GetMapping("/compare-chart/plratio")
    public ResponseEntity<Map<Integer, Map<LocalDateTime, Long>>> getPlRatio(
            @RequestParam List<Integer> aids
    ) {
        Map<Integer,Map<LocalDateTime,Long>> mapList = new HashMap<>();

        for(Integer aid : aids) {
            mapList.put(aid,accountService.getPlRatio(aid));
        }

        log.info(String.format("[GET] /compare-chart/plratio/aids=%s",aids));

        return new ResponseEntity<>(mapList, HttpStatus.OK);
    }
}
