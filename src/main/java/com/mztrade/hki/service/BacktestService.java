package com.mztrade.hki.service;

import com.mztrade.hki.entity.Bar;
import com.mztrade.hki.entity.Position;
import com.mztrade.hki.entity.backtest.BacktestHistory;
import com.mztrade.hki.entity.backtest.BacktestRequest;
import com.mztrade.hki.entity.backtest.Condition;
import com.mztrade.hki.repository.BacktestHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
public class BacktestService {
    private AccountService accountService;
    private StockPriceService stockPriceService;
    private OrderService orderService;
    private BacktestHistoryRepository backtestHistoryRepository;

    @Autowired
    public BacktestService(AccountService accountService,
                           StockPriceService stockPriceService,
                           OrderService orderService,
                           BacktestHistoryRepository backtestHistoryRepository) {
        this.accountService = accountService;
        this.stockPriceService = stockPriceService;
        this.orderService = orderService;
        this.backtestHistoryRepository = backtestHistoryRepository;
    }

    public int execute(
            int uid,
            long initialBalance,
            List<List<Condition>> buyConditions,
            List<List<Condition>> sellConditions,
            List<Float> dca,
            int maxTradingCount,
            List<String> targetTickers,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        // Account Settings
        int aid = accountService.createAccount(uid);
        accountService.deposit(aid, initialBalance);

        // Portfolio Settings
        long maxSingleTickerTradingBalance = initialBalance / maxTradingCount;
        log.info("maxSingleTickerTradingBalance: " + maxSingleTickerTradingBalance);
        Map<String, Integer> dcaStatus = new HashMap<>();
        for (String ticker : targetTickers) {
            dcaStatus.put(ticker, 0);
        }
        log.info("dcaStatus: " + dcaStatus);

        // Get Bars
        Map<String, List<Bar>> collectedBars = new HashMap<>();
        boolean buyFlag = false;
        boolean sellFlag = false;

        log.info("Start backtesting from " + startDate + " to " + endDate);

        for (; startDate.isBefore(endDate); startDate = startDate.plus(1, ChronoUnit.DAYS)) {
            for (String ticker : targetTickers) {
                // Stacking retrievable bar
                try {
                    Bar bar = stockPriceService.getPrice(ticker, startDate);
                    log.info("bar: " + bar);
                    if (!collectedBars.containsKey(ticker)) {
                        collectedBars.put(ticker, new ArrayList<>());
                    }
                    collectedBars.get(ticker).add(bar);
                } catch (EmptyResultDataAccessException e) {
                    log.error("barError: " + e);
                    continue;
                }
                // if 'any' bar collected in a particular ticker ... continue downwards

                // check buy conditions and buy
                for (List<Condition> buyConditionGroup : buyConditions) {
                    log.info("buyCondition for loop depths 1");
                    // check concurrent trading limit
                    if (!orderService.getPositions(aid).contains(ticker) && orderService.getPositions(aid).size() >= maxTradingCount) {
                        log.info("condition 1: " + !orderService.getPositions(aid).contains(ticker));
                        log.info("condition 2: " + (orderService.getPositions(aid).size() >= maxTradingCount));
                        log.info("condition sum: " + (!orderService.getPositions(aid).contains(ticker) && orderService.getPositions(aid).size() >= maxTradingCount));
                        continue;
                    }
                    buyFlag = true;
                    for (Condition buyCondition : buyConditionGroup) {
                        log.info("buyCondition for loop depths 2a");
                        if (!buyCondition.check(collectedBars.get(ticker))) {
                            buyFlag = false;
                        }
                    }
                    if (buyFlag) {
                        log.info("buyFlag on");
                        if (dcaStatus.get(ticker) < dca.size()) {
                            double targetBuyAmount = dca.get(dcaStatus.get(ticker)) * maxSingleTickerTradingBalance;
                            int currentPrice = collectedBars.get(ticker).get(collectedBars.size() - 1).getClose();
                            int targetQty = (int) Math.floor(targetBuyAmount / currentPrice);
                            if (targetQty > 0) {
                                orderService.buy(aid, ticker, startDate, targetQty);
                                dcaStatus.replace(ticker, dcaStatus.get(ticker) + 1);
                            }
                        }
                    }
                }
                // Check sell conditions and sell
                for (List<Condition> sellConditionGroup : sellConditions) {
                    sellFlag = true;
                    for (Condition sellCondition : sellConditionGroup) {
                        if (!sellCondition.check(collectedBars.get(ticker))) {
                            sellFlag = false;
                        }
                    }
                    if (sellFlag) {
                        Optional<Position> position = orderService.getPosition(aid, ticker);
                        if (position.isPresent()) {
                            orderService.sell(aid, ticker, startDate, position.get().getQty());
                        }
                        dcaStatus.replace(ticker, 0);
                    }
                }
            }
            /*
            if (!orderService.getPositions(aid).isEmpty()) {
                System.out.println("===============================================");
                System.out.println("Date: " + startDate);
                System.out.println("Current Balance: " + accountService.getBalance(aid));
                System.out.println("Current Positions ...");
                for (Position position : orderService.getPositions(aid)) {
                    System.out.println("   Ticker: " + position.getTicker());
                    System.out.println("   Entry Price: " + position.getAvgEntryPrice());
                    System.out.println("   Qty: " + position.getQty());
                }
                System.out.println("===============================================");
            }*/
        }
        return aid;
    }

    public boolean create(BacktestHistory backtestHistory) {
        return backtestHistoryRepository.create(backtestHistory);
    }

    public BacktestHistory get(int aid) {
        return backtestHistoryRepository.get(aid);
    }

    public List<BacktestHistory> getRanking() {
        return backtestHistoryRepository.getRanking();
    }

    public BacktestRequest getBacktestRequest(int aid) throws NoSuchElementException {
        return backtestHistoryRepository.getBacktestRequest(aid).orElseThrow();
    }
    public List<String> getTradedTickers(int aid) {
        return getBacktestRequest(aid).getTickers();
    }

    public List<BacktestHistory> searchByTitle(int uid,String title) {
        return backtestHistoryRepository.searchByTitle(uid, title);
    }

    public Integer getNumberOfHistoryByUid(int uid) {
        return backtestHistoryRepository.getNumberOfHistoryByUid(uid);
    }

    public Optional<Integer> getHighestProfitLossRatio(int uid) {
        double highestProfitLossRatio = -1;
        Optional<Integer> highestAid = Optional.empty();
        for (int aid : accountService.getAll(uid)) {
            double currentProfitLossRatio = backtestHistoryRepository.get(aid).getPlratio();
            if (currentProfitLossRatio > highestProfitLossRatio) {
                highestProfitLossRatio = currentProfitLossRatio;
                highestAid = Optional.of(aid);
            }
        }
        return highestAid;
    }

    public Double calculateFinalProfitLossRatio(long initialBalance, int aid, LocalDateTime backtestEndDate) {
        long finalBalance = accountService.getBalance(aid);
        List<Position> positions = orderService.getPositions(aid);
        for (Position position : positions) {
            Integer finalClosePrice = stockPriceService.getAvailablePriceBefore(position.getTicker(), backtestEndDate, 10).orElseThrow().getClose();
            finalBalance += (long) position.getQty() * finalClosePrice;
        }
        return (finalBalance / (double) initialBalance) - 1;
    }
}
