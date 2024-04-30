package com.mztrade.hki.outdated;

import com.mztrade.hki.Util;
import com.mztrade.hki.entity.StockPrice;
import com.mztrade.hki.entity.backtest.Condition;
import com.mztrade.hki.entity.backtest.Indicator;
import com.mztrade.hki.service.StockPriceService;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@Sql(scripts = {"classpath:schema.sql", "classpath:db/stock_info_1205.sql", "classpath:db/stock_price_1205.sql"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class ConditionTests {
    @Autowired
    private StockPriceService stockPriceService;

    @Test
    @Order(1)
    void simpleConditionTest() {
        Condition c = new Condition()
                .setBaseIndicator(new Indicator("RSI", List.of(10f)))
                .setConstantBound(75f)
                .setCompareType(">");
        List<StockPrice> stockPrices = stockPriceService.getPrices("000270",
                LocalDateTime.parse(Util.formatDate("20220101")),
                LocalDateTime.parse(Util.formatDate("20221231")));

        int mCount = 0;
        for (int i = 0; i < 200; i++) {
            if (c.check(stockPrices.subList(i, i+30))) mCount++;
        }
        Assertions.assertThat(mCount).isEqualTo(10);
    }

    @Test
    @Order(2)
    void fullConditionTest() {
        Condition c = new Condition()
                .setBaseIndicator(new Indicator("RSI", List.of(10f)))
                .setConstantBound(70f)
                .setCompareType(">")
                .setFrequency(List.of(10, 5));
        List<StockPrice> stockPrices = stockPriceService.getPrices("000270",
                LocalDateTime.parse(Util.formatDate("20220101")),
                LocalDateTime.parse(Util.formatDate("20221231")));

        int mCount = 0;
        for (int i = 0; i < 200; i++) {
            if (c.check(stockPrices.subList(i, i+30))) mCount++;}
        Assertions.assertThat(mCount).isEqualTo(15);
    }

    @Test
    @Order(3)
    void twoIndicatorConditionTest() {
        Condition c = new Condition()
                .setBaseIndicator(new Indicator("SMA", List.of(5f)))
                .setTargetIndicator(new Indicator("SMA", List.of(100f)))
                .setConstantBound(0f)
                .setCompareType(">")
                .setFrequency(List.of(7, 2));
        List<StockPrice> stockPrices = stockPriceService.getPrices("000270",
                LocalDateTime.parse(Util.formatDate("20200101")),
                LocalDateTime.parse(Util.formatDate("20221231")));
    }
}
