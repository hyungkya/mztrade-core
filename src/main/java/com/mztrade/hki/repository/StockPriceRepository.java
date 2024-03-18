package com.mztrade.hki.repository;

import com.mztrade.hki.entity.Bar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockPriceRepository extends JpaRepository<Bar, Integer> {
    List<Bar> findByTicker(String ticker);
    Optional<Bar> findByTickerAndDate(String ticker, LocalDateTime date);
    List<Bar> findByTickerAndDateBetween(String ticker, LocalDateTime startDate, LocalDateTime endDate);
}
