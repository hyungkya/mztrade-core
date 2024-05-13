package com.mztrade.hki.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.Util;
import com.mztrade.hki.dto.BacktestParameter;
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

        BacktestParameter backtestParameter = backtestService.getBacktestParameter(aid);
        for (String tradedTicker : backtestParameter.getTickers()) {
            benchmarkProfits.put(tradedTicker, getTickerBenchmarkProfit(tradedTicker,
                    backtestParameter.parseStartDate(),
                    backtestParameter.parseEndDate()));
        }
        return benchmarkProfits;
    }

    public double getTickerBenchmarkProfit(String ticker, LocalDateTime startDate, LocalDateTime endDate) {

        double BenchmarkProfit = ((double)stockPriceService.getAvailablePriceBefore(ticker, endDate).orElseThrow().getClose() /
                stockPriceService.getAvailablePriceAfter(ticker, startDate).orElseThrow().getClose()) - 1;

        return BenchmarkProfit;
    }

    public Map<String, Double> getTickerProfit(int aid) {
        Map<String, Double> profit = new HashMap<>();
        List<String> tradedTickers = backtestService.getTradedTickers(aid);
        for (String tradedTicker : tradedTickers) {
            profit.put(tradedTicker, getTickerProfit(aid, tradedTicker));
        }
        return profit;
    }

    public Double getTickerProfit(int aid, String ticker) {
        double totalProfitLoss = 0;
        for (Order order : orderService.getSellOrderHistory(aid, ticker)) {
            totalProfitLoss +=
            ((double) order.getQty() * order.getPrice()) - (order.getQty() * order.getAvgEntryPrice().doubleValue());
        }

        double profit = totalProfitLoss / backtestService.getBacktestParameter(aid).getInitialBalance();
        return profit;
    }

    public Double getTickerAlphaProfit(int aid, String ticker) {
        BacktestParameter backtestParameter = backtestService.getBacktestParameter(aid);
        double absoluteProfitLoss = getTickerProfit(aid, ticker);
        double benchmarkProfitLoss = getTickerBenchmarkProfit(
                ticker,
                backtestParameter.parseStartDate(),
                backtestParameter.parseEndDate());

        double profit = absoluteProfitLoss - benchmarkProfitLoss;
        return profit;
    }

    public Map<String, Double> getTickerAlphaProfit(int aid) {
        Map<String, Double> alphaProfits = new HashMap<>();
        BacktestParameter backtestParameter = backtestService.getBacktestParameter(aid);
        for (String tradedTicker : backtestParameter.getTickers()) {
            alphaProfits.put(tradedTicker, getTickerAlphaProfit(aid, tradedTicker));
        }
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

        return rate;
    }

    public Double getTickerTradeFrequency(int aid, String ticker) {
        int totalCount = 0;
        int tradeCount = orderService.getOrderHistory(aid,ticker).size();
        BacktestParameter br = backtestService.getBacktestParameter(aid);

        totalCount += stockPriceService.getPrices(ticker
                ,LocalDateTime.parse(Util.formatDate(br.getStartDate()))
                ,LocalDateTime.parse(Util.formatDate(br.getEndDate()))
        ).size();

        double TradeFrequency = (double) tradeCount / (double) (totalCount);
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

        return rate;
    }

    public Double getTradeFrequency(int aid) {
        int totalCount = 0;
        int tradeCount = orderService.getOrderHistory(aid).size();

        BacktestParameter br = backtestService.getBacktestParameter(aid);
        for(String ticker : br.getTickers()) {
            totalCount += stockPriceService.getPrices(ticker
                    ,LocalDateTime.parse(Util.formatDate(br.getStartDate()))
                    ,LocalDateTime.parse(Util.formatDate(br.getEndDate()))
                    ).size();
        }

        double TradeFrequency = (double) tradeCount / (double) (totalCount);
        return TradeFrequency;
    }
}
