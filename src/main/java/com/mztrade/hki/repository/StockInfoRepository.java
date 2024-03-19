package com.mztrade.hki.repository;

import com.mztrade.hki.entity.StockInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockInfoRepository extends JpaRepository<StockInfo, Integer> {
    StockInfo getByTicker(String ticker);
    Optional<StockInfo> findByTicker(String ticker);
    List<StockInfo> findAllByNameContainsIgnoreCase(String name);
}
