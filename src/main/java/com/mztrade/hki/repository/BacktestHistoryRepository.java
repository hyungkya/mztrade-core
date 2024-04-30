package com.mztrade.hki.repository;

import com.mztrade.hki.entity.backtest.BacktestHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BacktestHistoryRepository extends JpaRepository<BacktestHistory, Integer> {
    BacktestHistory save(BacktestHistory backtestHistory);
    List<BacktestHistory> findTop5ByUserUidOrderByPlratioDesc(Integer uid);
    List<BacktestHistory> findTop5ByOrderByPlratioDesc();
    Integer countByUserUid(Integer uid);
    @Query("SELECT b FROM BacktestHistory b WHERE b.user.uid = :uid AND CAST(FUNCTION('JSON_EXTRACT', b.param, '$.title') AS STRING) like CONCAT('%', :title, '%')")
    List<BacktestHistory> searchByTitle(@Param("uid") int uid, @Param("title") String title);
}
