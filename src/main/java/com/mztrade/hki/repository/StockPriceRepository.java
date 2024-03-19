package com.mztrade.hki.repository;

import com.mztrade.hki.entity.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockPriceRepository extends JpaRepository<StockPrice, Integer> {
    List<StockPrice> findByStockInfoTicker(String ticker);
    Optional<StockPrice> findByStockInfoTickerAndDate(String ticker, LocalDateTime date);
    List<StockPrice> findByStockInfoTickerAndDateBetween(String ticker, LocalDateTime startDate, LocalDateTime endDate);
}
