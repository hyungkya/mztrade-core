package com.mztrade.hki.repository;


import com.mztrade.hki.entity.StockInfoTag;
import com.mztrade.hki.entity.StockInfoTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockInfoTagRepository extends JpaRepository<StockInfoTag, StockInfoTagId> {
    StockInfoTag save(StockInfoTag stockInfoTag);
}
