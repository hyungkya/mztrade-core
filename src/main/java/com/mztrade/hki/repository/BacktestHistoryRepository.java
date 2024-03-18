package com.mztrade.hki.repository;

import com.mztrade.hki.entity.Account;
import com.mztrade.hki.entity.backtest.BacktestHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BacktestHistoryRepository extends JpaRepository<BacktestHistory, Integer> {
    BacktestHistory save(BacktestHistory backtestHistory);

}
