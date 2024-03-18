package com.mztrade.hki.repository;

import com.mztrade.hki.entity.GameHistory;
import com.mztrade.hki.entity.GameOrderHistory;
import com.mztrade.hki.entity.GameOrderHistoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameOrderRepository extends JpaRepository<GameOrderHistory, GameOrderHistoryId> {
    GameOrderHistory save(GameOrderHistory gameOrderHistory);


}
