package com.mztrade.hki.service;

import com.mztrade.hki.Util;
import com.mztrade.hki.entity.Account;
import com.mztrade.hki.entity.Bar;
import com.mztrade.hki.entity.GameHistory;
import com.mztrade.hki.entity.StockInfo;
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

    @Autowired
    public GameService(
            GameRepository gameRepository,
            AccountRepository accountRepository,

            StockPriceService stockPriceService
    ) {
        this.gameRepository = gameRepository;
        this.accountRepository = accountRepository;
        this.stockPriceService = stockPriceService;
    }

    public int createGame(int aid) {
        List<StockInfo> stockInfoList = stockPriceService.getAllStockInfo();

        Random random = new Random();

        String ticker = stockInfoList.get(random.nextInt(stockInfoList.size())).getTicker();

        List<Bar> bars = stockPriceService.getPrices(ticker);
        bars = bars.stream().skip(200).limit(200).toList();

        LocalDateTime startDate = bars.get(random.nextInt(bars.size())).getDate();
        int gid = gameRepository.createGame(aid,ticker, startDate);
        log.debug(String.format("createGame(aid: %d) -> gid: %d", aid, gid));

        return gid;
    }

    public List<Account> getAccounts(int uid) {
        List<Account> accounts = accountRepository.getGameAccount(uid);
        log.debug(String.format("getAccounts(uid: %d) -> accounts: %s", uid, accounts));
        return accounts;
    }

    public List<GameHistory> getGameHistories(int aid) {
        List<GameHistory> gameHistories = gameRepository.getGameHistory(aid);
        log.debug(String.format("getGameHistories(aid: %d) -> gameHistories: %s", aid, gameHistories));
        return gameHistories;
    }
}
