/*
package com.mztrade.hki.repo;

import com.mztrade.hki.entity.Order;
import com.mztrade.hki.repository.OrderHistoryRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
*/
