package com.mztrade.hki.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.entity.GameHistory;
import com.mztrade.hki.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.time.LocalDateTime;
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

    public void updateGame(Integer gid, Integer turns, Integer maxTurn, Boolean finished) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("gid", gid, Types.INTEGER)
                .addValue("turns", turns, Types.INTEGER)
                .addValue("max_turn", maxTurn, Types.INTEGER)
                .addValue("finished", finished, Types.BOOLEAN);
        this.template.update(
                "UPDATE hkidb.game_history SET turns = :turns, max_turn = :max_turn, finished = :finished WHERE gid = :gid",
                src);
    }

    public List<GameHistory> getGameHistoryByAccountId(int aid) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("aid", aid, Types.INTEGER);
        return this.template.query(
                "SELECT * FROM hkidb.game_history WHERE aid = :aid",
                src,
                (rs, rowNum) -> GameHistory.builder()
                        .aid(rs.getInt("aid"))
                        .gid(rs.getInt("gid"))
                        .turns(rs.getInt("turns"))
                        .maxTurn(rs.getInt("max_turn"))
                        .ticker(rs.getString("ticker"))
                        .startDate(rs.getTimestamp("start_date").toLocalDateTime())
                        .plratio(rs.getDouble("plratio"))
                        .finished(rs.getBoolean("finished"))
                        .build()
        );
    }

    public List<GameHistory> getGameHistoryByGameId(int gid) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("gid", gid, Types.INTEGER);
        return this.template.query(
                "SELECT * FROM hkidb.game_history WHERE gid = :gid",
                src,
                (rs, rowNum) -> GameHistory.builder()
                        .aid(rs.getInt("aid"))
                        .gid(rs.getInt("gid"))
                        .turns(rs.getInt("turns"))
                        .maxTurn(rs.getInt("max_turn"))
                        .ticker(rs.getString("ticker"))
                        .startDate(rs.getTimestamp("start_date").toLocalDateTime())
                        .plratio(rs.getDouble("plratio"))
                        .finished(rs.getBoolean("finished"))
                        .build()
        );
    }

    public Boolean createGameOrderHistory(Integer oid, Integer gid) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("oid", oid, Types.INTEGER)
                .addValue("gid", gid, Types.INTEGER);
        int rowsAffected = this.template.update(
                "INSERT INTO hkidb.game_order_history (oid, gid) VALUES (:oid, :gid)",
                src);
        return rowsAffected == 1;
    }

    public List<Order> getGameOrderHistories(Integer gid) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("gid", gid, Types.INTEGER);
        return this.template.query(
                "SELECT * FROM hkidb.game_order_history go INNER JOIN hkidb.order_history o ON go.oid = o.oid WHERE go.gid = :gid",
                src,
                (rs, rowNum) -> Order.builder()
                        .aid(rs.getInt("aid"))
                        .avgEntryPrice(rs.getBigDecimal("avg_entry_price"))
                        .filledTime(rs.getTimestamp("filled_time").toLocalDateTime())
                        .ticker(rs.getString("ticker"))
                        .qty(rs.getInt("qty"))
                        .price(rs.getInt("price"))
                        .oid(rs.getInt("oid"))
                        .otid(rs.getInt("otid"))
                        .build());
    }
}
