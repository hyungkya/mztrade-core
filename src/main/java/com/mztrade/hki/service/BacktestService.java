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
        Map<String, Integer> dcaStatus = new HashMap<>();
        for (String ticker : targetTickers) {
            dcaStatus.put(ticker, 0);
        }

        // Get Bars
        Map<String, List<Bar>> collectedBars = new HashMap<>();
        boolean buyFlag = false;
        boolean sellFlag = false;

        for (; startDate.isBefore(endDate); startDate = startDate.plus(1, ChronoUnit.DAYS)) {
            for (String ticker : targetTickers) {
                // Stacking retrievable bar
                try {
                    Bar bar = stockPriceService.getPrice(ticker, startDate);
                    if (!collectedBars.containsKey(ticker)) {
                        collectedBars.put(ticker, new ArrayList<>());
                    }
                    collectedBars.get(ticker).add(bar);
                } catch (EmptyResultDataAccessException e) {
                    log.trace("[BacktestService] " + e);
                    continue;
                }
                // if 'any' bar collected in a particular ticker ... continue downwards

                // check buy conditions and buy
                for (List<Condition> buyConditionGroup : buyConditions) {
                    // check concurrent trading limit
                    if (!orderService.getPositions(aid).contains(ticker) && orderService.getPositions(aid).size() >= maxTradingCount) {
                        continue;
                    }
                    buyFlag = true;
                    for (Condition buyCondition : buyConditionGroup) {
                        if (!buyCondition.check(collectedBars.get(ticker))) {
                            buyFlag = false;
                        }
                    }
                    if (buyFlag) {
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
        }
        log.debug(String.format("[BacktestService] execute(uid: %d, initialBalance: %d, buyConditions: %s, " +
                "sellConditions: %s, dca: %s, maxTradingCount: %d, targetTickers: %s, " +
                "startDate: %s, endDate: %s) -> aid: %d", uid, initialBalance, buyConditions, sellConditions, dca,
                maxTradingCount, targetTickers, startDate, endDate, aid));
        return aid;
    }

    public boolean create(BacktestHistory backtestHistory) {
        boolean isSuccess = backtestHistoryRepository.create(backtestHistory);;
        log.debug(String.format("[BacktestService] create(backtestHistory: %s) -> isSuccess: %b", backtestHistory, isSuccess));
        return isSuccess;
    }

    public BacktestHistory get(int aid) {
        BacktestHistory backtestHistory = backtestHistoryRepository.get(aid);
        log.debug(String.format("[BacktestService] get(int: %d) -> backtestHistory: %s", aid, backtestHistory));
        return backtestHistory;
    }

    public List<BacktestHistory> getRanking() {
        List<BacktestHistory> backtestHistories = backtestHistoryRepository.getRanking();
        log.debug(String.format("[BacktestService] getRanking() -> backtestHistories: %s", backtestHistories));
        return backtestHistories;
    }

    public BacktestRequest getBacktestRequest(int aid) throws NoSuchElementException {
        BacktestRequest backtestRequest = backtestHistoryRepository.getBacktestRequest(aid).orElseThrow();
        log.debug(String.format("[BacktestService] getBacktestRequest(aid: %d) -> backtestRequest: %s", aid, backtestRequest));
        return backtestRequest;
    }
    public List<String> getTradedTickers(int aid) {
        List<String> tickers = getBacktestRequest(aid).getTickers();
        log.debug(String.format("[BacktestService] getTradedTickers(aid: %d) -> tickers: %s", aid, tickers));
        return tickers;
    }

    public List<BacktestHistory> searchByTitle(int uid, String title) {
        List<BacktestHistory> backtestHistories = backtestHistoryRepository.searchByTitle(uid, title);
        log.debug(String.format("[BacktestService] searchByTitle(uid: %d, title: %s) -> backtestHistories: %s", uid, title, backtestHistories));
        return backtestHistories;
    }

    public List<BacktestHistory> searchBacktestHistoryByTags(int uid, String title, List<Integer> tids) {
        List<BacktestHistory> backtestHistories = backtestHistoryRepository.findBacktestHistoryByTitleAndTags(uid, title, tids);
        log.debug(String.format("[BacktestService] searchBacktestHistoryByTags(uid: %d, title: %s, tids: %s) -> backtestHistories: %s", uid, title, tids, backtestHistories));
        return backtestHistories;
    }

    public Integer getNumberOfHistoryByUid(int uid) {
        int num = backtestHistoryRepository.getNumberOfHistoryByUid(uid);
        log.debug(String.format("[BacktestService] getNumberOfHistoryByUid(uid: %d) -> num: %d", uid, num));
        return num;
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
        log.debug(String.format("[BacktestService] getHighestProfitLossRatio(uid: %d) -> highestAid: %s", uid, highestAid));
        return highestAid;
    }

    public Double calculateFinalProfitLossRatio(long initialBalance, int aid, LocalDateTime backtestEndDate) {
        long finalBalance = accountService.getBalance(aid);
        List<Position> positions = orderService.getPositions(aid);
        for (Position position : positions) {
            Integer finalClosePrice = stockPriceService.getAvailablePriceBefore(position.getTicker(), backtestEndDate, 10).orElseThrow().getClose();
            finalBalance += (long) position.getQty() * finalClosePrice;
        }
        double plratio = (finalBalance / (double) initialBalance) - 1;
        log.debug(String.format("[BacktestService] calculateFinalProfitLossRatio(initialBalance: %d, aid: %d, " +
                "backtestEndDate: %s) -> plratio: %f", initialBalance, aid, backtestEndDate, plratio));
        return plratio;
    }
}
