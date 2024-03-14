package com.mztrade.hki.service;

import com.mztrade.hki.entity.*;
import com.mztrade.hki.repository.AccountRepository;
import com.mztrade.hki.repository.GameRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class GameService {
    private final GameRepository gameRepository;
    private final AccountRepository accountRepository;
    private final StockPriceService stockPriceService;
    private final OrderService orderService;

    @Autowired
    public GameService(
            GameRepository gameRepository,
            AccountRepository accountRepository,
            OrderService orderService,
            StockPriceService stockPriceService
    ) {
        this.gameRepository = gameRepository;
        this.accountRepository = accountRepository;
        this.orderService = orderService;
        this.stockPriceService = stockPriceService;
    }

    public int createGame(int aid) {
        List<StockInfo> stockInfoList = stockPriceService.getAllStockInfo();

        Random random = new Random();

        String ticker = stockInfoList.get(random.nextInt(stockInfoList.size())).getTicker();

        List<Bar> bars = stockPriceService.getPrices(ticker);
        bars = bars.stream().skip(200).limit(200).toList();

        LocalDateTime startDate = bars.get(random.nextInt(bars.size())).getDate();

        long balance = accountRepository.getBalance(aid);

        int gid = gameRepository.createGame(aid, ticker, startDate, balance);
        log.debug(String.format("createGame(aid: %d) -> gid: %d", aid, gid));

        return gid;
    }

    public List<Account> getAccounts(int uid) {
        List<Account> accounts = accountRepository.getGameAccount(uid);
        log.debug(String.format("getAccounts(uid: %d) -> accounts: %s", uid, accounts));
        return accounts;
    }

    public List<GameHistory> getGameHistoryByAccountId(int aid) {
        List<GameHistory> gameHistories = gameRepository.getGameHistoryByAccountId(aid);
        log.debug(String.format("getGameHistoryByAccountId(aid: %d) -> gameHistories: %s", aid, gameHistories));
        return gameHistories;
    }

    public List<GameHistory> getGameHistoryByGameId(int gid) {
        List<GameHistory> gameHistories = gameRepository.getGameHistoryByGameId(gid);
        log.debug(String.format("getGameHistoryByGameId(gid: %d) -> gameHistories: %s", gid, gameHistories));
        return gameHistories;
    }

    public GameHistory getUnFinishedGameHistory(int aid) {
        GameHistory gameHistory = gameRepository.getUnFinishedGameHistory(aid);
        log.debug(String.format("getUnFinishedGameHistory(gid: %d) -> gameHistory: %s", aid, gameHistory));
        return gameHistory;
    }

    public Boolean sell(Integer gid, Integer aid, String ticker, LocalDateTime date, Integer qty) {
        Boolean isProcessed = false;
        Integer oid = orderService.sell(aid, ticker, date, qty);
        if (oid != null) {
            isProcessed = gameRepository.createGameOrderHistory(oid, gid);
        }
        log.debug(String.format("sell(gid: %d, aid: %d, ticker: %s, date: %s, qty: %d) -> isProcessed: %b", gid, aid, ticker, date, qty, isProcessed));
        return isProcessed;
    }

    public Boolean buy(Integer gid, Integer aid, String ticker, LocalDateTime date, Integer qty) {
        Boolean isProcessed = false;
        Integer oid = orderService.buy(aid, ticker, date, qty);
        if (oid != null) {
            isProcessed = gameRepository.createGameOrderHistory(oid, gid);
        }
        log.debug(String.format("buy(gid: %d, aid: %d, ticker: %s, date: %s, qty: %d) -> isProcessed: %b", gid, aid, ticker, date, qty, isProcessed));
        return isProcessed;
    }

    public List<Order> getGameOrderHistories(Integer gid) {
        List<Order> orders = gameRepository.getGameOrderHistories(gid);
        log.debug(String.format("getGameOrderHistories(gid: %d) -> orders: %s", gid, orders));
        return orders;
    }

    public void increaseTurns(Integer gid) {
        GameHistory gameHistory = gameRepository.getGameHistoryByGameId(gid).getFirst();
        gameRepository.updateGame(gid, gameHistory.getTurns() + 1, gameHistory.getMaxTurn(), false);
        log.debug(String.format("increaseGameTurns(gid: %d)", gid));
    }

    public void updateMaxTurn(Integer gid, Integer extraTurns) {
        GameHistory gameHistory = gameRepository.getGameHistoryByGameId(gid).getFirst();
        gameRepository.updateGame(gid, gameHistory.getTurns(), gameHistory.getMaxTurn() + extraTurns, false);
        log.debug(String.format("increaseGameTurns(gid: %d)", gid));
    }

    public void finishGame(Integer gid) {
        GameHistory gameHistory = gameRepository.getGameHistoryByGameId(gid).getFirst();
        gameRepository.updateGame(gid, gameHistory.getTurns(), gameHistory.getMaxTurn(), true);
        log.debug(String.format("finishGame(gid: %d)", gid));
    }
}
