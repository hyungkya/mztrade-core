package com.mztrade.hki.controller;

import com.mztrade.hki.entity.Account;
import com.mztrade.hki.entity.GameHistory;
import com.mztrade.hki.entity.Tag;
import com.mztrade.hki.service.GameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public List<Account> getGameAccount(
            @RequestParam Integer uid
    ) {
        List<Account> accounts = gameService.getAccounts(uid);
        log.info(String.format("[GET] /game/account/uid=%s", uid));
        return accounts;
    }

    @GetMapping("/game")
    public List<GameHistory> getGameHistories(
            @RequestParam Integer aid
    ) {
        List<GameHistory> gameHistories = gameService.getGameHistories(aid);
        log.info(String.format("[GET] /game?aid=%s", aid));
        return gameHistories;
    }


}
