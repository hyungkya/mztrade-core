package com.mztrade.hki.outdated;

import com.mztrade.hki.Util;
import com.mztrade.hki.entity.Position;
import com.mztrade.hki.entity.Order;
import com.mztrade.hki.service.AccountService;
import com.mztrade.hki.service.OrderService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootTest
@Sql(scripts = {"classpath:db/schema.sql", "classpath:db/stock_info_1205.sql", "classpath:db/stock_price_1205.sql"})

public class OrderTests {
    @Autowired
    private AccountService accountService;

    @Autowired
    private OrderService orderService;

    @Test
    void simpleBuyTest() {
        accountService.createAccount(1);
        accountService.deposit(1, 50_000_000L);

        orderService.buy(1, "000270", LocalDateTime.parse(Util.formatDate("20150105")), 3);
        orderService.buy(1, "000270", LocalDateTime.parse(Util.formatDate("20150112")), 3);
        orderService.buy(1, "000270", LocalDateTime.parse(Util.formatDate("20150119")), 3);

        Position position = orderService.getPositions(1).get(0);
        BigDecimal theOneThatShouldBeInitialMoney = position.getAvgEntryPrice().multiply(BigDecimal.valueOf(position.getQty())).add(BigDecimal.valueOf(accountService.getBalance(1)));
        Assertions.assertThat(theOneThatShouldBeInitialMoney.compareTo(BigDecimal.valueOf(50_000_000L))).isEqualTo(0);

        orderService.sell(1, "000270", LocalDateTime.parse(Util.formatDate("201501230")), 9);
        Assertions.assertThat(orderService.getPositions(1).size()).isEqualTo(0);
    }
}
