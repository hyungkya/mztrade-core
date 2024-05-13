package com.mztrade.hki.controller;

import com.mztrade.hki.dto.AccountResponse;
import com.mztrade.hki.dto.GameHistoryResponse;
import com.mztrade.hki.dto.GameRanking;
import com.mztrade.hki.dto.OrderResponse;
import com.mztrade.hki.service.GameService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        log.info(String.format("[POST] /game?aid=%s", aid));
        List<GameHistoryResponse> gameHistoryResponses = gameService.getUnFinishedGameHistory(aid);
        if (gameHistoryResponses.isEmpty()) {
            int gid = gameService.createGame(aid);
            return new ResponseEntity<>(gid, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(gameHistoryResponses.getFirst().getGid(), HttpStatus.OK);
        }
    }

    @GetMapping("/game/account")
    public List<AccountResponse> getGameAccount(
            @RequestParam Integer uid
    ) {
        List<AccountResponse> accountResponses = gameService.getAccounts(uid);
        log.info(String.format("[GET] /game/account?uid=%s", uid));
        return accountResponses;
    }
    @GetMapping("/game/{gid}")
    public List<GameHistoryResponse> getGameHistory(
            @PathVariable Integer gid
    ) {
        List<GameHistoryResponse> gameHistoryResponses;
        gameHistoryResponses = gameService.getGameHistoryByGameId(gid);
        log.info(String.format("[GET] /game/%s", gid));
        return gameHistoryResponses;
    }

    @GetMapping("/game")
    public List<GameHistoryResponse> getGameHistories(
            @RequestParam Integer aid
    ) {
        List<GameHistoryResponse> gameHistoryResponses;
        gameHistoryResponses = gameService.getGameHistoryByAccountId(aid);
        log.info(String.format("[GET] /game?aid=%s", aid));
        return gameHistoryResponses;
    }

    @GetMapping("/game/ranking")
    public List<GameRanking> getGameRanking() {
        List<GameRanking> gameRanking = gameService.getGameRanking();
        log.info("[GET] /game/ranking");
        return gameRanking;
    }

    @GetMapping("/game/un-finished")
    public List<GameHistoryResponse> getUnFinishedGameHistory(
            @RequestParam Integer aid
    ) {
        List<GameHistoryResponse> gameHistoryResponses = gameService.getUnFinishedGameHistory(aid);
        log.info(String.format("[GET] /game/un-finished?aid=%s", aid));
        return gameHistoryResponses;
    }

    @PostMapping("/game/{gid}/order/buy")
    public ResponseEntity<Boolean> processBuyOrder(
            @PathVariable Integer gid,
            @RequestParam Integer aid,
            @RequestParam String ticker,
            @RequestParam LocalDateTime date,
            @RequestParam Integer qty
    ) {
        log.info(String.format("[POST] /game/%s/order/buy?aid=%s&ticker=%s&date=%s&qty=%s", gid, aid, ticker, date, qty));
        return new ResponseEntity<>(gameService.buy(gid, aid, ticker, date, qty), HttpStatus.OK);
    }
    @PostMapping("/game/{gid}/order/sell")
    public ResponseEntity<Boolean> processSellOrder(
            @PathVariable Integer gid,
            @RequestParam Integer aid,
            @RequestParam String ticker,
            @RequestParam LocalDateTime date,
            @RequestParam Integer qty
    ) {
        log.info(String.format("[POST] /game/%s/order/sell?aid=%s&ticker=%s&date=%s&qty=%s", gid, aid, ticker, date, qty));
        return new ResponseEntity<>(gameService.sell(gid, aid, ticker, date, qty), HttpStatus.OK);
    }

    @GetMapping("/game/{gid}/order")
    public ResponseEntity<List<OrderResponse>> getGameOrderHistories(
            @PathVariable Integer gid
    ) {
        log.info(String.format("[GET] /game/order?gid=%d", gid));
        return new ResponseEntity<>(gameService.getGameOrderHistories(gid), HttpStatus.OK);
    }

    @PostMapping("/game/{gid}/turns")
    public ResponseEntity<Integer> increaseGameTurns(
            @PathVariable Integer gid
    ) {
        Integer currentTurn = gameService.increaseTurns(gid);
        log.info(String.format("[POST] /game/%s/turns", gid));
        return new ResponseEntity<>(currentTurn, HttpStatus.OK);
    }

    @PostMapping("/game/{gid}/max-turn")
    public ResponseEntity<Boolean> updateGameMaxTurn(
            @PathVariable Integer gid,
            @RequestParam Integer amount
    ) {
        gameService.updateMaxTurn(gid, amount);
        log.info(String.format("[POST] /game/%s/max-turn?amount=%d", gid, amount));
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @PostMapping("/game/{gid}/finish")
    public ResponseEntity<Boolean> finishGame(
            @PathVariable Integer gid
    ) {
        gameService.finishGame(gid);
        log.info(String.format("[POST] /game/%s/finish", gid));
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}
