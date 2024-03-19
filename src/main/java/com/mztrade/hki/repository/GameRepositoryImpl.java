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

    public Integer createGame(Integer aid, String ticker, LocalDateTime startDate, Long balance) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("aid", aid, Types.INTEGER)
                .addValue("ticker", ticker, Types.VARCHAR)
                .addValue("start_date", startDate, Types.TIMESTAMP)
                .addValue("start_balance", balance, Types.BIGINT);
        this.template.update(
                "INSERT INTO hkidb.game_history (aid, ticker, start_date, start_balance) VALUES (:aid, :ticker, :start_date, :start_balance)",
                src,
                keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    public void updateGame(Integer gid, Integer turns, Integer maxTurn, Long finalBalance, Boolean finished) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("gid", gid, Types.INTEGER)
                .addValue("turns", turns, Types.INTEGER)
                .addValue("max_turn", maxTurn, Types.INTEGER)
                .addValue("final_balance", finalBalance, Types.BIGINT)
                .addValue("finished", finished, Types.BOOLEAN);
        this.template.update(
                "UPDATE hkidb.game_history SET turns = :turns, max_turn = :max_turn, finished = :finished, final_balance = :final_balance WHERE gid = :gid",
                src);
    }

    public List<GameHistory> getGameHistoryByAccountId(int aid) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("aid", aid, Types.INTEGER);
        return this.template.query(
                "SELECT * FROM hkidb.game_history WHERE aid = :aid AND finished = true",
                src,
                (rs, rowNum) -> GameHistory.builder()
                        .aid(rs.getInt("aid"))
                        .gid(rs.getInt("gid"))
                        .turns(rs.getInt("turns"))
                        .maxTurn(rs.getInt("max_turn"))
                        .ticker(rs.getString("ticker"))
                        .startDate(rs.getTimestamp("start_date").toLocalDateTime())
                        .startBalance(rs.getLong("start_balance"))
                        .finalBalance(rs.getLong("final_balance"))
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
                        .startBalance(rs.getLong("start_balance"))
                        .finalBalance(rs.getLong("final_balance"))
                        .finished(rs.getBoolean("finished"))
                        .build()
        );
    }

    public List<GameHistory> getUnFinishedGameHistory(int aid) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("aid", aid, Types.INTEGER);
        return this.template.query(
                "SELECT * FROM hkidb.game_history WHERE aid = :aid AND finished = false",
                src,
                (rs, rowNum) -> GameHistory.builder()
                        .aid(rs.getInt("aid"))
                        .gid(rs.getInt("gid"))
                        .turns(rs.getInt("turns"))
                        .maxTurn(rs.getInt("max_turn"))
                        .ticker(rs.getString("ticker"))
                        .startDate(rs.getTimestamp("start_date").toLocalDateTime())
                        .startBalance(rs.getLong("start_balance"))
                        .finalBalance(rs.getLong("final_balance"))
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
                        .account(accountRepository.getById(rs.getInt("aid")))
                        .avgEntryPrice(rs.getBigDecimal("avg_entry_price"))
                        .filledTime(rs.getTimestamp("filled_time").toLocalDateTime())
                        .stockInfo(stockInfoRepository.getByTicker(rs.getString("ticker")))
                        .qty(rs.getInt("qty"))
                        .price(rs.getInt("price"))
                        .oid(rs.getInt("oid"))
                        .otid(rs.getInt("otid"))
                        .build());
    }
}
