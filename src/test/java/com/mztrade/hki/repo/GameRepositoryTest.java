/*
package com.mztrade.hki.repo;

import com.mztrade.hki.entity.GameHistory;
import com.mztrade.hki.repository.GameRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
        List<GameHistory> gameHistories = gameRepository.findByAccountAidAndFinished(1,true);
        for(GameHistory gameHistory : gameHistories) {
            System.out.println(gameHistory);
        }
    }

    @Test
    void findByAidAndFinishedFalse() {
        List<GameHistory> gameHistories = gameRepository.findByAccountAidAndFinished(1,false);
        for(GameHistory gameHistory : gameHistories) {
            System.out.println(gameHistory);
        }
    }
}
*/
