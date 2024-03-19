package com.mztrade.hki.controller;

import com.mztrade.hki.dto.AccountResponse;
import com.mztrade.hki.dto.OrderResponse;
import com.mztrade.hki.entity.Account;
import com.mztrade.hki.entity.GameHistory;
import com.mztrade.hki.entity.Order;
import com.mztrade.hki.service.GameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
public class GameController {
    private GameService gameService;


    @Autowired GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/game")
    public ResponseEntity<Integer> createGame(
            @RequestParam Integer aid
    ) {
        int gid = gameService.createGame(aid);
        log.info(String.format("[POST] /game/aid=%s", aid));
        return new ResponseEntity<>(gid, HttpStatus.OK);
    }

    @GetMapping("/game/account")
    public List<AccountResponse> getGameAccount(
            @RequestParam Integer uid
    ) {
        List<AccountResponse> accountResponses = gameService.getAccounts(uid);
        log.info(String.format("[GET] /game/account/uid=%s", uid));
        return accountResponses;
    }

    @GetMapping("/game")
    public List<GameHistory> getGameHistories(
            @RequestParam(required = false) Integer aid,
            @RequestParam(required = false) Integer gid
    ) {
        assert aid == null ^ gid == null;
        List<GameHistory> gameHistories;
        if (aid != null) {
            gameHistories = gameService.getGameHistoryByAccountId(aid);
            log.info(String.format("[GET] /game?aid=%s", aid));
        } else {
            gameHistories = gameService.getGameHistoryByGameId(gid);
            log.info(String.format("[GET] /game?gid=%s", gid));
        }
        return gameHistories;
    }

    @GetMapping("/game/un-finished")
    public List<GameHistory> getUnFinishedGameHistory(
            @RequestParam Integer aid
    ) {
        List<GameHistory> gameHistories = gameService.getUnFinishedGameHistory(aid);
        log.info(String.format("[GET] /game/un-finished?aid=%s", aid));
        return gameHistories;
    }

    @PostMapping("/game/order/buy")
    public ResponseEntity<Boolean> processBuyOrder(
            @RequestParam Integer gid,
            @RequestParam Integer aid,
            @RequestParam String ticker,
            @RequestParam LocalDateTime date,
            @RequestParam Integer qty
    ) {
        log.info(String.format("[POST] /game/order/buy?aid=%s&ticker=%s&date=%s&qty=%s", aid, ticker, date, qty));
        return new ResponseEntity<>(gameService.buy(gid, aid, ticker, date, qty), HttpStatus.OK);
    }
    @PostMapping("/game/order/sell")
    public ResponseEntity<Boolean> processSellOrder(
            @RequestParam Integer gid,
            @RequestParam Integer aid,
            @RequestParam String ticker,
            @RequestParam LocalDateTime date,
            @RequestParam Integer qty
    ) {
        log.info(String.format("[POST] /game/order/sell?aid=%s&ticker=%s&date=%s&qty=%s", aid, ticker, date, qty));
        return new ResponseEntity<>(gameService.sell(gid, aid, ticker, date, qty), HttpStatus.OK);
    }

    @GetMapping("/game/order")
    public ResponseEntity<List<OrderResponse>> getGameOrderHistories(
            @RequestParam Integer gid
    ) {
        log.info(String.format("[GET] /game/order?gid=%d", gid));
        return new ResponseEntity<>(gameService.getGameOrderHistories(gid), HttpStatus.OK);
    }

    @PostMapping("/game/turns")
    public ResponseEntity<Boolean> increaseGameTurns(
            @RequestParam Integer gid
    ) {
        boolean bool = gameService.increaseTurns(gid);
        log.info(String.format("[POST] /game/turns?gid=%d", gid));
        return new ResponseEntity<>(bool, HttpStatus.OK);
    }

    @PostMapping("/game/max-turn")
    public ResponseEntity<Boolean> updateGameMaxTurn(
            @RequestParam Integer gid,
            @RequestParam Integer extraTurn
    ) {
        gameService.updateMaxTurn(gid, extraTurn);
        log.info(String.format("[POST] /game/max-turn?gid=%d&extraTurn=%d", gid, extraTurn));
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @PostMapping("/game/finish")
    public ResponseEntity<Boolean> finishGame(
            @RequestParam Integer gid
    ) {
        gameService.finishGame(gid);
        log.info(String.format("[POST] /game/finish?gid=%d", gid));
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}
