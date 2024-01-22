package com.mztrade.hki.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.Util;
import com.mztrade.hki.entity.Bar;
import com.mztrade.hki.entity.Order;
import com.mztrade.hki.entity.backtest.BacktestRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
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
    public StatisticService(OrderService orderService, BacktestService backtestService) {
        this.orderService = orderService;
        this.backtestService = backtestService;
        this.stockPriceService = stockPriceService;
    }

    public Map<String, Double> getTickerProfit(int aid) {
        Map<String, Double> profits = new HashMap<>();
        List<String> tradedTickers = backtestService.getTradedTickers(aid);
        for (String tradedTicker : tradedTickers) {
            BigDecimal buyAmount = BigDecimal.valueOf(0L);
            BigDecimal sellAmount = BigDecimal.valueOf(0L);
            for (Order order : orderService.getSellOrderHistory(aid, tradedTicker)) {
                buyAmount = buyAmount.add(
                        BigDecimal.valueOf(order.getQty()).multiply(order.getAvgEntryPrice())
                );
                sellAmount = sellAmount.add(
                        BigDecimal.valueOf(order.getQty()).multiply(BigDecimal.valueOf(order.getPrice()))
                );
            }
            if (buyAmount.equals(0L)) {
                profits.put(tradedTicker, null);
            } else {
                profits.put(tradedTicker, sellAmount.doubleValue() / buyAmount.doubleValue());
            }

        }
        return profits;
    }

    public Double getTickerProfit(int aid, String ticker) {
        BigDecimal buyAmount = BigDecimal.valueOf(0L);
        BigDecimal sellAmount = BigDecimal.valueOf(0L);
        for (Order order : orderService.getSellOrderHistory(aid, ticker)) {
            buyAmount = buyAmount.add(
                    BigDecimal.valueOf(order.getQty()).multiply(order.getAvgEntryPrice())
            );
            sellAmount = sellAmount.add(
                    BigDecimal.valueOf(order.getQty()).multiply(BigDecimal.valueOf(order.getPrice()))
            );
        }
        if (buyAmount.equals(0L)) {
            return Double.NaN;
        } else {
            return sellAmount.doubleValue() / buyAmount.doubleValue();
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
        int tradeCount = 0;
        int totalCount = orderService.getOrderHistory(aid).size();

        BacktestRequest br = backtestService.getBacktestHistory(aid);
        for(String ticker : br.getTickers()) {
            tradeCount += stockPriceService.getPrices(ticker
                    ,Instant.parse(Util.formatDate(br.getStartDate()))
                    ,Instant.parse(Util.formatDate(br.getEndDate()))
                    ).size();
        }

        if (totalCount == 0) {
            return Double.NaN;
        } else {
            return (double) tradeCount / (double) (totalCount * br.getTickers().size());
        }
        //TODO:: 매매 빈도 조회 기능 추가하기
    }
}
