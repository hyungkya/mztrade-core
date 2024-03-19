package com.mztrade.hki.repository;

import com.mztrade.hki.entity.Position;
import com.mztrade.hki.entity.PositionId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, PositionId> {
    Position save(Position position);
    @Transactional
    void deleteByAccountAidAndStockInfoTicker(Integer aid, String ticker);
    Optional<Position> findByAccountAidAndStockInfoTicker(Integer aid, String ticker);
    List<Position> findByAccountAid(Integer aid);
}
