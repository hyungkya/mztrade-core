package com.mztrade.hki;

import com.mztrade.hki.entity.Tag;
import com.mztrade.hki.entity.TagCategory;
import com.mztrade.hki.entity.User;
import com.mztrade.hki.entity.backtest.BacktestRequest;
import com.mztrade.hki.repository.BacktestHistoryRepository;
import com.mztrade.hki.repository.TagRepository;
import com.mztrade.hki.repository.UserRepository;
import com.mztrade.hki.service.OrderService;
import com.mztrade.hki.service.StatisticService;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ExampleTests {
    @Autowired
    private BacktestHistoryRepository backtestHistoryRepository;
    @Autowired
    private OrderService orderService;
    @Autowired
    private StatisticService statisticService;

    @Autowired
    private TagRepository tagRepository;


    @Test
    void getBacktestRequestTest() {
        BacktestRequest br = backtestHistoryRepository.getBacktestRequest(1).get();
        assertThat(br.getTickers())
                .isEqualTo(List.of("000270", "000660", "003670", "005380", "005490"));
    }

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

    @Test
    void simpleTagTest() {
        tagRepository.createTag(1, "매매법1", "0xFFFFFFFF", TagCategory.STOCK_INFO);
        tagRepository.createTag(1, "매매법2", "0xFFFFFFFF", TagCategory.STOCK_INFO);
        tagRepository.createTag(1, "종목분류1", "0xFFFFFFFF", TagCategory.STOCK_INFO);
        tagRepository.createTag(1, "종목분류2", "0xFFFFFFFF", TagCategory.STOCK_INFO);

        tagRepository.findByCategory(1, TagCategory.STOCK_INFO);

        tagRepository.deleteById(0);
        tagRepository.deleteById(1);

        tagRepository.findByCategory(1, TagCategory.STOCK_INFO);
    }

}
