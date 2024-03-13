package com.mztrade.hki.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.entity.AccountHistory;
import com.mztrade.hki.entity.GameHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class GameRepository {
    private final NamedParameterJdbcTemplate template;
    private final ObjectMapper objectMapper;

    @Autowired
    public GameRepository(NamedParameterJdbcTemplate template, ObjectMapper objectMapper) {
        this.template = template;
        this.objectMapper = objectMapper;
    }

    public Integer createGame(Integer aid, String ticker, LocalDateTime startDate) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("aid", aid, Types.INTEGER)
                .addValue("ticker", ticker, Types.VARCHAR)
                .addValue("start_date", startDate, Types.TIMESTAMP);
        this.template.update(
                "INSERT INTO hkidb.game_history (aid, ticker, start_date) VALUES (:aid, :ticker, :start_date)",
                src,
                keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    public List<GameHistory> getGameHistory(int aid) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("aid", aid, Types.INTEGER);
        return this.template.query(
                "SELECT * FROM hkidb.game_history WHERE aid = :aid",
                src,
                (rs, rowNum) -> GameHistory.builder()
                        .aid(rs.getInt("aid"))
                        .gid(rs.getInt("gid"))
                        .turns(rs.getInt("turns"))
                        .ticker(rs.getString("ticker"))
                        .startDate(rs.getTimestamp("start_date").toLocalDateTime())
                        .plratio(rs.getDouble("plratio"))
                        .build()
        );
    }

}
