package com.mztrade.hki.repository;

import com.mztrade.hki.entity.DailyStockPrice;
import com.mztrade.hki.entity.MinutelyStockPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MinutelyStockPriceRepository extends JpaRepository<MinutelyStockPrice, Integer> {
    List<MinutelyStockPrice> findByStockInfoTicker(String ticker);
    Optional<MinutelyStockPrice> findByStockInfoTickerAndDate(String ticker, LocalDateTime date);
    List<MinutelyStockPrice> findByStockInfoTickerAndDateBetween(String ticker, LocalDateTime startDate, LocalDateTime endDate);
}
