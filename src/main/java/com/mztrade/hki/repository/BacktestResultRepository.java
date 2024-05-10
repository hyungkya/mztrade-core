package com.mztrade.hki.repository;

import com.mztrade.hki.entity.backtest.BacktestResult;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BacktestResultRepository extends JpaRepository<BacktestResult, Integer> {
    BacktestResult save(BacktestResult backtestResult);
    List<BacktestResult> findTop5ByUserUidOrderByPlratioDesc(Integer uid);
    List<BacktestResult> findTop5ByOrderByPlratioDesc();
    Integer countByUserUid(Integer uid);
    @Query("SELECT b FROM BacktestResult b WHERE b.user.uid = :uid AND CAST(FUNCTION('JSON_EXTRACT', b.param, '$.title') AS STRING) like CONCAT('%', :title, '%')")
    List<BacktestResult> searchByTitle(@Param("uid") int uid, @Param("title") String title);
}
