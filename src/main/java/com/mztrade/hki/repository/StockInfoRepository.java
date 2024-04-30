package com.mztrade.hki.repository;

import com.mztrade.hki.entity.StockInfo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockInfoRepository extends JpaRepository<StockInfo, Integer> {
    StockInfo getByTicker(String ticker);
    Optional<StockInfo> findByTicker(String ticker);
    List<StockInfo> findAllByNameContainsIgnoreCase(String name);
}
