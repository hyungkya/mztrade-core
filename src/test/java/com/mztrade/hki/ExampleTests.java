/*
package com.mztrade.hki;


import static org.assertj.core.api.Assertions.assertThat;

import com.mztrade.hki.repository.TagRepositoryImpl;
import com.mztrade.hki.service.OrderService;
import com.mztrade.hki.service.StatisticService;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ExampleTests {
    @Autowired
    private OrderService orderService;
    @Autowired
    private StatisticService statisticService;

    @Autowired
    private TagRepositoryImpl tagRepositoryImpl;


    @Test
    void getAllBuyOrderHistoryTest() {
        assertThat(orderService.getBuyOrderHistory(1).size())
                .isEqualTo(526);
    }

    @Test
    void getAllSellOrderHistoryTest() {
        assertThat(orderService.getSellOrderHistory(1).size())
                .isEqualTo(239);
    }

    @Test
    void getIndividualBuyOrderHistoryTest() {
        assertThat(orderService.getBuyOrderHistory(1, "005380").size())
                .isEqualTo(101);
    }

    @Test
    void getIndividualSellOrderHistoryTest() {
        assertThat(orderService.getSellOrderHistory(1, "005380").size())
                .isEqualTo(47);
    }

    @Test
    void getAllTickerProfitTest() {
        assertThat(statisticService.getTickerProfit(1).size())
                .isEqualTo(5);
    }

    @Test
    void getIndividualTickerProfitTest() {
        assertThat(
                statisticService.getTickerProfit(1, "005380")
        ).isCloseTo(0.0547521, Offset.offset(0.0000001));
    }

}
*/
