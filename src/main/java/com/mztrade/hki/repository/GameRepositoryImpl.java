package com.mztrade.hki.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.dto.GameRanking;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class GameRepositoryImpl {
    private final NamedParameterJdbcTemplate template;
    private final ObjectMapper objectMapper;
    private final AccountRepository accountRepository;
    private final StockInfoRepository stockInfoRepository;

    @Autowired
    public GameRepositoryImpl(NamedParameterJdbcTemplate template, ObjectMapper objectMapper, AccountRepository accountRepository, StockInfoRepository stockInfoRepository) {
        this.template = template;
        this.objectMapper = objectMapper;
        this.accountRepository = accountRepository;
        this.stockInfoRepository = stockInfoRepository;
    }

    public List<GameRanking> getGameRanking() {
        MapSqlParameterSource src = new MapSqlParameterSource();
        return this.template.query(
                "SELECT c.name, gh.final_balance " +
                        "FROM customers c " +
                        "INNER JOIN account a ON c.uid = a.uid " +
                        "INNER JOIN (SELECT gh1.* FROM game_history gh1 " +
                        "            INNER JOIN (SELECT aid, MAX(gid) AS max_gid " +
                        "                        FROM game_history " +
                        "                        WHERE finished = true " +
                        "                        GROUP BY aid) gh2 ON gh1.aid = gh2.aid AND gh1.gid = gh2.max_gid) gh " +
                        "ON a.aid = gh.aid " +
                        "WHERE a.type = 'GAME' " +
                        "ORDER BY gh.final_balance DESC " +
                        "LIMIT 5;",
                src,
                (rs, rowNum) -> GameRanking.builder()
                        .name(rs.getString("name"))
                        .finalBalance(rs.getLong("final_balance"))
                        .build());
    }
}
