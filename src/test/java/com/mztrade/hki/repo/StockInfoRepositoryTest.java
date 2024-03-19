package com.mztrade.hki.repo;

import com.mztrade.hki.entity.StockInfo;
import com.mztrade.hki.repository.StockInfoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class StockInfoRepositoryTest {
    @Autowired
    StockInfoRepository stockInfoRepository;
    @Test
    void findByTicker() {
        Optional<StockInfo> stockInfo = stockInfoRepository.findByTicker("000270");
        System.out.println(stockInfo.isPresent());
        System.out.println(stockInfo.get());
    }

    @Test
    void findAllByNameContainsIgnoreCase() {
        List<StockInfo> stockInfos = stockInfoRepository.findAllByNameContainsIgnoreCase("삼성");
        System.out.println(stockInfos.size());
        stockInfos.forEach((e) -> System.out.println(e));
    }

    @Test
    void getAll() {
        List<StockInfo> stockInfos = stockInfoRepository.findAll();
        System.out.println(stockInfos.size());
        stockInfos.forEach((e) -> System.out.println(e));
    }
}
