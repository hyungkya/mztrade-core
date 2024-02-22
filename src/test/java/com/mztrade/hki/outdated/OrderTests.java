package com.mztrade.hki.outdated;

import com.mztrade.hki.Util;
import com.mztrade.hki.entity.Position;
import com.mztrade.hki.service.AccountService;
import com.mztrade.hki.service.OrderService;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootTest
@Sql(scripts = {"classpath:db/schema.sql", "classpath:db/data.sql"})
public class OrderTests {
    @Autowired
    private AccountService accountService;

    @Autowired
    private OrderService orderService;

    @Test
    void simpleBuyTest() {
        accountService.createAccount(1);
        accountService.deposit(2, 50_000_000L);

        orderService.buy(2, "000270", LocalDateTime.parse(Util.formatDate("20150105")), 3);
        orderService.buy(2, "000270", LocalDateTime.parse(Util.formatDate("20150112")), 3);
        orderService.buy(2, "000270", LocalDateTime.parse(Util.formatDate("20150119")), 3);

        Position position = orderService.getPositions(2).get(0);
        BigDecimal theOneThatShouldBeInitialMoney = position.getAvgEntryPrice().multiply(BigDecimal.valueOf(position.getQty())).add(BigDecimal.valueOf(accountService.getBalance(2)));
        Assertions.assertThat(theOneThatShouldBeInitialMoney).isCloseTo(BigDecimal.valueOf(50_000_000L), Percentage.withPercentage(0.00001));

        orderService.sell(2, "000270", LocalDateTime.parse(Util.formatDate("201501230")), 9);
        Assertions.assertThat(orderService.getPositions(2).size()).isEqualTo(0);
    }
}
