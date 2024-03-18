package com.mztrade.hki.service;

import com.mztrade.hki.entity.*;
import com.mztrade.hki.repository.GameOrderRepository;
import com.mztrade.hki.repository.GameRepository;
import com.mztrade.hki.repository.GameRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class GameService {
    private final GameRepositoryImpl gameRepositoryImpl;
    private final GameRepository gameRepository;
    private final GameOrderRepository gameOrderRepository;
    private final AccountService accountService;
    private final StockPriceService stockPriceService;
    private final OrderService orderService;

    @Autowired
    public GameService(
            GameRepository gameRepository,
            GameOrderRepository gameOrderRepository,
            GameRepositoryImpl gameRepositoryImpl,
            AccountService accountService,
            OrderService orderService,
            StockPriceService stockPriceService
    ) {
        this.gameRepository = gameRepository;
        this.gameOrderRepository = gameOrderRepository;
        this.gameRepositoryImpl = gameRepositoryImpl;
        this.accountService = accountService;
        this.orderService = orderService;
        this.stockPriceService = stockPriceService;
    }

    public int createGame(int aid) {
        List<StockInfo> stockInfoList = stockPriceService.getAllStockInfo();

        Random random = new Random();

        String ticker = stockInfoList.get(random.nextInt(stockInfoList.size())).getTicker();

        List<Bar> bars = stockPriceService.getPrices(ticker);
        bars = bars.stream().skip(200).limit(bars.size() - 400).toList();

        LocalDateTime startDate = bars.get(random.nextInt(bars.size())).getDate();

        long balance = accountService.getBalance(aid);

        GameHistory gameHistory = gameRepository.save(GameHistory.builder()
                .aid(aid)
                .ticker(ticker)
                .startDate(startDate)
                .startBalance(balance).build());
        log.debug(String.format("createGame(aid: %d) -> gid: %d", aid, gameHistory.getGid()));

        return gameHistory.getGid();
    }

    public List<Account> getAccounts(int uid) {
        List<Account> accounts = accountService.getGameAccount(uid);
        log.debug(String.format("getAccounts(uid: %d) -> accounts: %s", uid, accounts));
        return accounts;
    }

    public List<GameHistory> getGameHistoryByAccountId(int aid) {
        List<GameHistory> gameHistories = gameRepository.findByAidAndFinished(aid,true);
        log.debug(String.format("getGameHistoryByAccountId(aid: %d) -> gameHistories: %s", aid, gameHistories));
        return gameHistories;
    }

    public List<GameHistory> getGameHistoryByGameId(int gid) {
        List<GameHistory> gameHistories = gameRepository.findByGid(gid);
        log.debug(String.format("getGameHistoryByGameId(gid: %d) -> gameHistories: %s", gid, gameHistories));
        return gameHistories;
    }

    public List<GameHistory> getUnFinishedGameHistory(int aid) {
        List<GameHistory> gameHistories = gameRepository.findByAidAndFinished(aid,false);
        log.debug(String.format("getUnFinishedGameHistory(gid: %d) -> gameHistories: %s", aid, gameHistories));
        return gameHistories;
    }

    public Boolean sell(Integer gid, Integer aid, String ticker, LocalDateTime date, Integer qty) {
        Boolean isProcessed = false;
        Integer oid = orderService.sell(aid, ticker, date, qty);
        if (oid != null) {
            GameOrderHistory gameOrderHistory = gameOrderRepository.save(
                    GameOrderHistory.builder()
                            .gid(gid)
                            .oid(oid).build());

            if (gameOrderHistory != null) {
                isProcessed = true;
            }
        }
        log.debug(String.format("sell(gid: %d, aid: %d, ticker: %s, date: %s, qty: %d) -> isProcessed: %b", gid, aid, ticker, date, qty, isProcessed));
        return isProcessed;
    }

    public Boolean buy(Integer gid, Integer aid, String ticker, LocalDateTime date, Integer qty) {
        Boolean isProcessed = false;
        Integer oid = orderService.buy(aid, ticker, date, qty);
        if (oid != null) {
            GameOrderHistory gameOrderHistory = gameOrderRepository.save(
                    GameOrderHistory.builder()
                            .gid(gid)
                            .oid(oid).build());

            if (gameOrderHistory != null) {
                isProcessed = true;
            }
        }
        log.debug(String.format("buy(gid: %d, aid: %d, ticker: %s, date: %s, qty: %d) -> isProcessed: %b", gid, aid, ticker, date, qty, isProcessed));
        return isProcessed;
    }

    public List<Order> getGameOrderHistories(Integer gid) {
        List<Order> orders = gameRepositoryImpl.getGameOrderHistories(gid);
        log.debug(String.format("getGameOrderHistories(gid: %d) -> orders: %s", gid, orders));
        return orders;
    }

    public boolean increaseTurns(Integer gid) {
        GameHistory gameHistory = gameRepository.findByGid(gid).getFirst();
        if(gameHistory.getTurns() < gameHistory.getMaxTurn()) {
            gameHistory.setTurns(gameHistory.getTurns() + 1);
            gameHistory.setMaxTurn(gameHistory.getMaxTurn());
            gameHistory.setFinalBalance(gameHistory.getFinalBalance());
            gameRepository.save(gameHistory);
            log.debug(String.format("increaseGameTurns(gid: %d)", gid));
            return false;
        } else {
            log.debug(String.format("increaseGameTurns(gid: %d)", gid));
            return true;
        }
    }

    public void updateMaxTurn(Integer gid, Integer extraTurns) {
        GameHistory gameHistory = gameRepository.findByGid(gid).getFirst();

        gameHistory.setTurns(gameHistory.getTurns());
        gameHistory.setMaxTurn(gameHistory.getMaxTurn() + extraTurns);
        gameHistory.setFinalBalance(gameHistory.getFinalBalance());
        gameHistory.setFinished(false);

        gameRepository.save(gameHistory);
        log.debug(String.format("increaseGameTurns(gid: %d)", gid));
    }

    public void finishGame(Integer gid) {
        GameHistory gameHistory = gameRepository.findByGid(gid).getFirst();
        long balance = accountService.getBalance(gameHistory.getAid());

        gameHistory.setTurns(gameHistory.getTurns());
        gameHistory.setMaxTurn(gameHistory.getMaxTurn());
        gameHistory.setFinalBalance(balance);
        gameHistory.setFinished(true);

        gameRepository.save(gameHistory);
        log.debug(String.format("finishGame(gid: %d)", gid));
    }
}
