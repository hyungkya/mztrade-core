package com.mztrade.hki.repository;


import com.mztrade.hki.entity.BacktestHistoryTag;
import com.mztrade.hki.entity.BacktestHistoryTagId;
import com.mztrade.hki.entity.StockInfoTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BacktestHistoryTagRepository extends JpaRepository<BacktestHistoryTag, BacktestHistoryTagId> {
    BacktestHistoryTag save(BacktestHistoryTag backtestHistoryTag);
}
