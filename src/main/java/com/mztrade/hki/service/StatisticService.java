package com.mztrade.hki.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.Util;
import com.mztrade.hki.dto.BacktestRequest;
import com.mztrade.hki.entity.Order;
import com.mztrade.hki.entity.OrderType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StatisticService {
    private OrderService orderService;
    private BacktestService backtestService;

    private StockPriceService stockPriceService;

    private ObjectMapper objectMapper;

    @Autowired
    public StatisticService(OrderService orderService, BacktestService backtestService, StockPriceService stockPriceService) {
        this.orderService = orderService;
        this.backtestService = backtestService;
        this.stockPriceService = stockPriceService;
    }

    public Map<String, Double> getTickerBenchmarkProfit(int aid) {
        Map<String, Double> benchmarkProfits = new HashMap<>();

        BacktestRequest backtestRequest = backtestService.getBacktestRequest(aid);
        for (String tradedTicker : backtestRequest.getTickers()) {
            benchmarkProfits.put(tradedTicker, getTickerBenchmarkProfit(tradedTicker,
                    backtestRequest.parseStartDate(),
                    backtestRequest.parseEndDate()));
        }

        log.debug(String.format("[StatisticService] getTickerBenchmarkProfit(aid: %s) -> benchmarkProfits:%s", aid,benchmarkProfits));

        return benchmarkProfits;
    }

    public double getTickerBenchmarkProfit(String ticker, LocalDateTime startDate, LocalDateTime endDate) {

        double BenchmarkProfit = ((double)stockPriceService.getAvailablePriceBefore(ticker, endDate).orElseThrow().getClose() /
                stockPriceService.getAvailablePriceAfter(ticker, startDate).orElseThrow().getClose()) - 1;

        log.debug(String.format("[StatisticService] getTickerBenchmarkProfit(ticker: %s, startDate: %s, endDate: %s) -> BenchmarkProfit:%s", ticker,startDate,endDate,BenchmarkProfit));

        return BenchmarkProfit;
    }

    public Map<String, Double> getTickerProfit(int aid) {
        Map<String, Double> profit = new HashMap<>();
        List<String> tradedTickers = backtestService.getTradedTickers(aid);
        for (String tradedTicker : tradedTickers) {
            profit.put(tradedTicker, getTickerProfit(aid, tradedTicker));
        }

        log.debug(String.format("[StatisticService] getTickerProfit(aid: %s) -> Profit:%s", aid,profit));
        return profit;
    }

    public Double getTickerProfit(int aid, String ticker) {
        double totalProfitLoss = 0;
        for (Order order : orderService.getSellOrderHistory(aid, ticker)) {
            totalProfitLoss +=
            ((double) order.getQty() * order.getPrice()) - (order.getQty() * order.getAvgEntryPrice().doubleValue());
        }

        double profit = totalProfitLoss / backtestService.getBacktestRequest(aid).getInitialBalance();

        log.debug(String.format("[StatisticService] getTickerProfit(aid: %s, ticker: %s) -> Profit:%s", aid, ticker,profit));
        return profit;
    }

    public Double getTickerAlphaProfit(int aid, String ticker) {
        BacktestRequest backtestRequest = backtestService.getBacktestRequest(aid);
        double absoluteProfitLoss = getTickerProfit(aid, ticker);
        double benchmarkProfitLoss = getTickerBenchmarkProfit(
                ticker,
                backtestRequest.parseStartDate(),
                backtestRequest.parseEndDate());

        double profit = absoluteProfitLoss - benchmarkProfitLoss;
        log.debug(String.format("[StatisticService] getTickerAlphaProfit(aid: %s, ticker: %s) -> profit:%s", aid, ticker,profit));
        return profit;
    }

    public Map<String, Double> getTickerAlphaProfit(int aid) {
        Map<String, Double> alphaProfits = new HashMap<>();
        BacktestRequest backtestRequest = backtestService.getBacktestRequest(aid);
        for (String tradedTicker : backtestRequest.getTickers()) {
            alphaProfits.put(tradedTicker, getTickerAlphaProfit(aid, tradedTicker));
        }
        log.debug(String.format("[StatisticService] getTickerAlphaProfit(aid: %s) -> alphaProfits:%s", aid,alphaProfits));
        return alphaProfits;
    }

    public Integer getTickerTradeCount(int aid, String ticker, int option) {
        int size;
        if (option == OrderType.BUY.id()) {
            size = orderService.getBuyOrderHistory(aid, ticker).size();
        } else if (option == OrderType.SELL.id()) {
            size = orderService.getSellOrderHistory(aid, ticker).size();
        } else {
            size = orderService.getOrderHistory(aid, ticker).size();
        }
        log.debug(String.format("[StatisticService] getTickerTradeCount(aid: %s, ticker: %s, option: %s) -> size:%s", aid,ticker,option,size));
        return size;
    }

    public Double getTickerTradingWinRate(int aid,String ticker) {
        int winRate = 0;
        int totalCount = 0;

        for (Order order : orderService.getSellOrderHistory(aid,ticker)) {
            BigDecimal price = BigDecimal.valueOf(order.getPrice().doubleValue());
            BigDecimal avgEntryPrice = BigDecimal.valueOf(order.getAvgEntryPrice().doubleValue());

            if (price.compareTo(avgEntryPrice) > 0) {
                winRate++;
            }

            totalCount++;
        }

        double rate;

        if (totalCount == 0) {
            rate = Double.NaN;
        } else {
            rate = (double)winRate / (double)totalCount;
        }

        log.debug(String.format("[StatisticService] getTradingWinRate(aid: %s) -> rate:%s", aid,rate));
        return rate;
    }

    public Double getTickerTradeFrequency(int aid, String ticker) {
        int totalCount = 0;
        int tradeCount = orderService.getOrderHistory(aid,ticker).size();
        BacktestRequest br = backtestService.getBacktestRequest(aid);

        totalCount += stockPriceService.getPrices(ticker
                ,LocalDateTime.parse(Util.formatDate(br.getStartDate()))
                ,LocalDateTime.parse(Util.formatDate(br.getEndDate()))
        ).size();

        double TradeFrequency = (double) tradeCount / (double) (totalCount);
        log.debug(String.format("[StatisticService] getTradeFrequency(aid: %s) -> TradeFrequency:%s", aid,TradeFrequency));
        return TradeFrequency;
    }

    public Double getTradingWinRate(int aid) {
        int winRate = 0;
        int totalCount = 0;

        for (Order order : orderService.getSellOrderHistory(aid)) {
                BigDecimal price = BigDecimal.valueOf(order.getPrice().doubleValue());
                BigDecimal avgEntryPrice = BigDecimal.valueOf(order.getAvgEntryPrice().doubleValue());

                if (price.compareTo(avgEntryPrice) > 0) {
                    winRate++;
                }

            totalCount++;
        }

        double rate;

        if (totalCount == 0) {
            rate = Double.NaN;
        } else {
            rate = (double)winRate / (double)totalCount;
        }

        log.debug(String.format("[StatisticService] getTradingWinRate(aid: %s) -> rate:%s", aid,rate));
        return rate;
    }

    public Double getTradeFrequency(int aid) {
        int totalCount = 0;
        int tradeCount = orderService.getOrderHistory(aid).size();

        BacktestRequest br = backtestService.getBacktestRequest(aid);
        for(String ticker : br.getTickers()) {
            totalCount += stockPriceService.getPrices(ticker
                    ,LocalDateTime.parse(Util.formatDate(br.getStartDate()))
                    ,LocalDateTime.parse(Util.formatDate(br.getEndDate()))
                    ).size();
        }

        double TradeFrequency = (double) tradeCount / (double) (totalCount);
        log.debug(String.format("[StatisticService] getTradeFrequency(aid: %s) -> TradeFrequency:%s", aid,TradeFrequency));
        return TradeFrequency;
    }
}
