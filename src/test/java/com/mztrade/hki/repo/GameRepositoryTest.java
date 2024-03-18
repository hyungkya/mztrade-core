package com.mztrade.hki.repo;

import com.mztrade.hki.Util;
import com.mztrade.hki.entity.Account;
import com.mztrade.hki.entity.AccountHistory;
import com.mztrade.hki.entity.GameHistory;
import com.mztrade.hki.repository.AccountHistoryRepository;
import com.mztrade.hki.repository.AccountRepository;
import com.mztrade.hki.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class GameRepositoryTest {
    @Autowired
    GameRepository gameRepository;
    @Test
    void findByGid() {
        List<GameHistory> gameHistories = gameRepository.findByGid(1);
        System.out.println(gameHistories);
    }

    @Test
    void findByAidAndFinishedTrue() {
        List<GameHistory> gameHistories = gameRepository.findByAidAndFinished(1,true);
        for(GameHistory gameHistory : gameHistories) {
            System.out.println(gameHistory);
        }
    }

    @Test
    void findByAidAndFinishedFalse() {
        List<GameHistory> gameHistories = gameRepository.findByAidAndFinished(1,false);
        for(GameHistory gameHistory : gameHistories) {
            System.out.println(gameHistory);
        }
    }
}
