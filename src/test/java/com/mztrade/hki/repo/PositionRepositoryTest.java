package com.mztrade.hki.repo;

import com.mztrade.hki.entity.Position;
import com.mztrade.hki.repository.PositionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@SpringBootTest
public class PositionRepositoryTest {
    @Autowired
    PositionRepository positionRepository;

    @Test
    void findByAidAndTicker() {
        Optional<Position> position = positionRepository.findByAccountAidAndStockInfoTicker(6, "000660");
        System.out.println(position.get());
    }

    @Test
    void findByAid() {
        List<Position> positions = positionRepository.findByAccountAid(9);
        System.out.println(positions);
    }

    @Test
    void saveAndDelete() {
        Position position = positionRepository.save(
                Position.builder()
                .aid(6)
                .qty(10)
                .avgEntryPrice(BigDecimal.valueOf(23000))
                .ticker("005930")
                .build()
        );
        System.out.println(positionRepository.findByAccountAid(6));
        positionRepository.deleteByAccountAidAndStockInfoTicker(6, "005930");
        System.out.println(positionRepository.findByAccountAid(6));
    }
}
