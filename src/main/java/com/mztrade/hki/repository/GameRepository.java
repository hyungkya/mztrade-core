package com.mztrade.hki.repository;

import com.mztrade.hki.entity.GameHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<GameHistory, Integer> {
    GameHistory save(GameHistory gameHistory);
    List<GameHistory> findByGid(Integer gid);
    List<GameHistory> findByAccountAidAndFinished(int aid, boolean finished);

}
