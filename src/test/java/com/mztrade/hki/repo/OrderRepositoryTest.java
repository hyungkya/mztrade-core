package com.mztrade.hki.repo;

import com.mztrade.hki.Util;
import com.mztrade.hki.entity.Account;
import com.mztrade.hki.entity.AccountHistory;
import com.mztrade.hki.entity.Order;
import com.mztrade.hki.repository.AccountHistoryRepository;
import com.mztrade.hki.repository.AccountRepository;
import com.mztrade.hki.repository.OrderHistoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
public class OrderRepositoryTest {
    @Autowired
    OrderHistoryRepository orderHistoryRepository;

    @Test
    void saveAndDelete() {
        Order order = orderHistoryRepository.save(
                Order.builder()
                        .qty(1)
                        .price(10000)
                        .otid(1)
                        .aid(6)
                        .filledTime(LocalDateTime.now())
                        .ticker("000270")
                        .build()
        );
        System.out.println(order);
    }
}
