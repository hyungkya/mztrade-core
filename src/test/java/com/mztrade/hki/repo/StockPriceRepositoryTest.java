package com.mztrade.hki.repo;

import com.mztrade.hki.Util;
import com.mztrade.hki.entity.Bar;
import com.mztrade.hki.repository.StockPriceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

@SpringBootTest
public class StockPriceRepositoryTest {
    @Autowired
    StockPriceRepository stockPriceRepository;
    @Test
    void findByTicker() {
        List<Bar> bars = stockPriceRepository.findByTicker("000270");
        System.out.println(bars.size());
        bars.stream().forEach((e) -> System.out.println(e));
    }

    @Test
    void findByTickerAndDateBetween() {
        List<Bar> bars = stockPriceRepository.findByTickerAndDateBetween("000270", Util.stringToLocalDateTime("20220101"), Util.stringToLocalDateTime("20221231"));
        System.out.println(bars.size());
        bars.stream().forEach((e) -> System.out.println(e));
    }

    @Test
    void findByTickerAndDate() {
        Optional<Bar> bar = stockPriceRepository.findByTickerAndDate("000270", Util.stringToLocalDateTime("20220103"));
        System.out.println(bar.isPresent());
        System.out.println(bar.get());
    }
}
