package com.mztrade.hki.repository;


import com.mztrade.hki.entity.BacktestResultTag;
import com.mztrade.hki.entity.BacktestResultTagId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BacktestResultTagRepository extends JpaRepository<BacktestResultTag, BacktestResultTagId> {
    BacktestResultTag save(BacktestResultTag backtestResultTag);
    List<BacktestResultTag> findByTagUserUidAndAccountAid(Integer uid, Integer aid);

}
