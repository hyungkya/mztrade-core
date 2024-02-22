package com.mztrade.hki.outdated;

import com.mztrade.hki.service.AccountService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@Sql(scripts = {"classpath:db/schema.sql"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class AccountTests {
    @Autowired
    private AccountService accountService;

    @Test
    @Order(1)
    void createAccount() {
        int aid = accountService.createAccount(1);
        Assertions.assertThat(aid).isEqualTo(1);
        long balance = accountService.getBalance(1);
        Assertions.assertThat(balance).isEqualTo(0L);
        accountService.deposit(1, 10000L);
        Assertions.assertThat(accountService.getBalance(1)).isEqualTo(10000L);
        accountService.withdraw(1, 10000L);
        Assertions.assertThat(accountService.getBalance(1)).isEqualTo(0L);
    }

}
