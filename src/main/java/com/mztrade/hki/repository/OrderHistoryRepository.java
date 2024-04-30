package com.mztrade.hki.repository;

import com.mztrade.hki.entity.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderHistoryRepository extends JpaRepository<Order, Integer> {
    Order save(Order order);
    List<Order> findByAccountAid(Integer aid);
    List<Order> findByAccountAidAndOtid(Integer aid, Integer otid);
    List<Order> findByAccountAidAndStockInfoTicker(Integer aid, String ticker);
    List<Order> findByAccountAidAndStockInfoTickerAndOtid(Integer aid, String ticker, Integer otid);
}
