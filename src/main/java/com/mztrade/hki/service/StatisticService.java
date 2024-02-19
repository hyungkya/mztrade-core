package com.mztrade.hki.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.Util;
import com.mztrade.hki.entity.Order;
import com.mztrade.hki.entity.OrderType;
import com.mztrade.hki.entity.backtest.BacktestRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
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
        return benchmarkProfits;
    }

    public double getTickerBenchmarkProfit(String ticker, LocalDateTime startDate, LocalDateTime endDate) {
        return ((double) stockPriceService.getAvailablePriceBefore(ticker, endDate).orElseThrow().getClose() /
                stockPriceService.getAvailablePriceAfter(ticker, startDate).orElseThrow().getClose()) - 1;
    }

    public Map<String, Double> getTickerProfit(int aid) {
        Map<String, Double> profits = new HashMap<>();
        List<String> tradedTickers = backtestService.getTradedTickers(aid);
        for (String tradedTicker : tradedTickers) {
            profits.put(tradedTicker, getTickerProfit(aid, tradedTicker));
        }
        return profits;
    }

    public Double getTickerProfit(int aid, String ticker) {
        double totalProfitLoss = 0;
        for (Order order : orderService.getSellOrderHistory(aid, ticker)) {
            totalProfitLoss +=
            ((double) order.getQty() * order.getPrice()) - (order.getQty() * order.getAvgEntryPrice().doubleValue());
        }
        return totalProfitLoss / Double.parseDouble(backtestService.getBacktestRequest(aid).getInitialBalance());
    }

    public Double getTickerAlphaProfit(int aid, String ticker) {
        BacktestRequest backtestRequest = backtestService.getBacktestRequest(aid);
        double absoluteProfitLoss = getTickerProfit(aid, ticker);
        double benchmarkProfitLoss = getTickerBenchmarkProfit(
                ticker,
                backtestRequest.parseStartDate(),
                backtestRequest.parseEndDate());
        return absoluteProfitLoss - benchmarkProfitLoss;
    }

    public Map<String, Double> getTickerAlphaProfit(int aid) {
        Map<String, Double> alphaProfits = new HashMap<>();
        BacktestRequest backtestRequest = backtestService.getBacktestRequest(aid);
        for (String tradedTicker : backtestRequest.getTickers()) {
            alphaProfits.put(tradedTicker, getTickerAlphaProfit(aid, tradedTicker));
        }
        return alphaProfits;
    }

    public Integer getTickerTradeCount(int aid, String ticker, int option) {
        if (option == OrderType.BUY.id()) {
            return orderService.getBuyOrderHistory(aid, ticker).size();
        } else if (option == OrderType.SELL.id()) {
            return orderService.getSellOrderHistory(aid, ticker).size();
        } else {
            return orderService.getOrderHistory(aid, ticker).size();
        }
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

        if (totalCount == 0) {
            return Double.NaN;
        } else {
            return (double)winRate / (double)totalCount;
        }
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
        System.out.print("total: " + totalCount);
        System.out.print("trade: " + tradeCount);
        return (double) tradeCount / (double) (totalCount);
        //TODO:: 매매 빈도 조회 기능 추가하기
    }
}
