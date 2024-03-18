package com.mztrade.hki.repository;

import com.mztrade.hki.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderHistoryRepository extends JpaRepository<Order, Integer> {
    Order save(Order order);
    List<Order> findByAid(Integer aid);
    List<Order> findByAidAndOtid(Integer aid, Integer otid);
    List<Order> findByAidAndTicker(Integer aid, String ticker);
    List<Order> findByAidAndTickerAndOtid(Integer aid, String ticker, Integer otid);
}
