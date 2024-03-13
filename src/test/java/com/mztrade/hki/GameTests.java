package com.mztrade.hki;


import com.mztrade.hki.repository.AccountRepository;
import com.mztrade.hki.repository.GameRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameTests {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private GameRepository gameRepository;

    @Test
    void getBacktestRequestTest() {
        int aid = accountRepository.createGameAccount(1);
        gameRepository.createGame(aid, "000270", Util.stringToLocalDateTime("20211115"));
    }
}
