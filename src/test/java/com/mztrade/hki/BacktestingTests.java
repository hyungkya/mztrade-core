package com.mztrade.hki;

import com.mztrade.hki.entity.backtest.Condition;
import com.mztrade.hki.entity.backtest.Indicator;
import com.mztrade.hki.service.BacktestService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Sql(scripts = {"classpath:db/schema.sql", "classpath:db/stock_info_1205.sql", "classpath:db/stock_price_1205.sql"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class BacktestingTests {
    @Autowired
    private BacktestService backtestService;

    @Test
    @Order(1)
    void simpleExecuteTest() {
        List<List<Condition>> buyConditions = new ArrayList<>();
        List<List<Condition>> sellConditions = new ArrayList<>();

        Condition buyCondition = new Condition()
                .setBaseIndicator(new Indicator("RSI", List.of(14f)))
                .setConstantBound(30f)
                .setCompareType("<");

        Condition sellCondition = new Condition()
                .setBaseIndicator(new Indicator("RSI", List.of(14f)))
                .setConstantBound(70f)
                .setCompareType(">");

        buyConditions.add(List.of(buyCondition));
        sellConditions.add(List.of(sellCondition));

        backtestService.execute(
                1,
                10_000_000L,
                buyConditions,
                sellConditions,
                List.of(0.33f, 0.33f, 0.34f),
                2,
                List.of("000270", "000660", "003670", "005380", "005490"),
                Instant.parse(Util.formatDate("20120101")),
                Instant.parse(Util.formatDate("20151231"))
        );
    }

}
