package com.mztrade.hki.repository;

import com.mztrade.hki.entity.DailyStockPrice;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyStockPriceRepository extends JpaRepository<DailyStockPrice, Integer> {
    List<DailyStockPrice> findByStockInfoTicker(String ticker);
    Optional<DailyStockPrice> findByStockInfoTickerAndDate(String ticker, LocalDateTime date);
    List<DailyStockPrice> findByStockInfoTickerAndDateBetween(String ticker, LocalDateTime startDate, LocalDateTime endDate);
}
