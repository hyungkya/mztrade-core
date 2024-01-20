package com.mztrade.hki.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticService {
    private OrderService orderService;
    private BacktestService backtestService;

    private ObjectMapper objectMapper;

    @Autowired
    public StatisticService(OrderService orderService, BacktestService backtestService) {
        this.orderService = orderService;
        this.backtestService = backtestService;
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

    public void getTradeFrequency(int aid) {
        //TODO:: 매매 빈도 조회 기능 추가하기
    }
}
