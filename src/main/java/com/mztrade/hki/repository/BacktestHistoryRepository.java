package com.mztrade.hki.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.entity.backtest.BacktestHistory;
import com.mztrade.hki.entity.backtest.BacktestRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Component
public class BacktestHistoryRepository {
    private final NamedParameterJdbcTemplate template;
    private final ObjectMapper objectMapper;

    @Autowired
    public BacktestHistoryRepository(NamedParameterJdbcTemplate template, ObjectMapper objectMapper) {
        this.template = template;
        this.objectMapper = objectMapper;
    }

    public boolean create(BacktestHistory backtestHistory) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("uid", backtestHistory.getUid(), Types.INTEGER)
                .addValue("aid", backtestHistory.getAid(), Types.INTEGER)
                .addValue("param", backtestHistory.getParam(), Types.VARCHAR)
                .addValue("plratio", backtestHistory.getPlratio(), Types.DOUBLE);
        try {
            this.template.update(
                    "INSERT INTO hkidb.backtest_history (uid, aid, param, plratio) VALUES (:uid, :aid, :param, :plratio)",
                    src);
        } catch (DataAccessException e) {
            return false;
        }
        return true;
    }

    public BacktestHistory get(int aid) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("aid", aid, Types.INTEGER);
        try {
            return this.template.queryForObject(
                    "SELECT b.aid, b.uid, b.param, b.plratio FROM hkidb.backtest_history b WHERE b.aid = :aid",
                    src,
                    (rs, rowNum) -> BacktestHistory.builder()
                            .uid(rs.getInt("b.uid"))
                            .aid(rs.getInt("b.aid"))
                            .param(rs.getString("b.param"))
                            .plratio(rs.getDouble("b.plratio"))
                            .build()
            );
        } catch (DataAccessException e) {
            return null;
        }
    }

    public List<BacktestHistory> getRanking() {
        MapSqlParameterSource src = new MapSqlParameterSource();
        try {
            return this.template.query(
                    "SELECT b.aid, b.uid, b.param, b.plratio FROM hkidb.backtest_history b ORDER BY b.plratio DESC LIMIT 5",
                    src,
                    (rs, rowNum) -> BacktestHistory.builder()
                            .uid(rs.getInt("b.uid"))
                            .aid(rs.getInt("b.aid"))
                            .param(rs.getString("b.param"))
                            .plratio(rs.getDouble("b.plratio"))
                            .build()
            );
        } catch (DataAccessException e) {
            return null;
        }
    }

    public Optional<BacktestRequest> getBacktestRequest(int aid) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("aid", aid, Types.INTEGER);
        Optional<BacktestRequest> backtestRequest;
        backtestRequest = Optional.of(
                this.template.queryForObject(
                "SELECT b.param FROM hkidb.backtest_history b WHERE b.aid = :aid",
                src,
                (rs, rowNum) -> {
                    try {
                        return objectMapper.readValue(rs.getString("b.param"), BacktestRequest.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
        ));
        return backtestRequest;
    }


    public List<BacktestHistory> searchByTitle(int uid, String title) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("uid", uid, Types.INTEGER)
                .addValue("title", "%" + title + "%", Types.VARCHAR);

        try {
            return this.template.query(
                    "SELECT b.aid, b.uid, b.param, b.plratio " +
                            "FROM hkidb.backtest_history b " +
                            "WHERE b.uid = :uid AND JSON_EXTRACT(b.param, '$.title') LIKE :title",
                    src,
                    (rs, rowNum) -> BacktestHistory.builder()
                            .uid(rs.getInt("b.uid"))
                            .aid(rs.getInt("b.aid"))
                            .param(rs.getString("b.param"))
                            .plratio(rs.getDouble("b.plratio"))
                            .build()
            );
        } catch (DataAccessException e) {
            return null;
        }
    }

    public Integer getNumberOfHistoryByUid(int uid) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("uid", uid, Types.INTEGER);
        try {
            return this.template.queryForObject(
                    "SELECT COUNT(*) FROM hkidb.backtest_history b WHERE b.uid = :uid",
                    src,
                    Integer.class);
        } catch (DataAccessException e) {
            return 0;
        }
    }

    public Integer deleteBacktestHistory(int aid) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("aid", aid, Types.INTEGER);
        return this.template.update(
                "DELETE FROM hkidb.backtest_history WHERE aid = :aid",
                src);
    }

    public List<BacktestHistory> findBacktestHistoryByTitleAndTags(int uid, String title, List<Integer> tids) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("uid", uid, Types.INTEGER)
                .addValue("title", "%" + title + "%", Types.VARCHAR)
                .addValue("tids", tids)
                .addValue("tid_length", tids.size(), Types.INTEGER);
        return this.template.query(
                "SELECT b.* " +
                        "FROM hkidb.backtest_history b " +
                        "WHERE JSON_EXTRACT(b.param, '$.title') LIKE :title AND b.aid IN ( " +
                        "    SELECT bit.aid " +
                        "    FROM hkidb.backtest_history_tag bit " +
                        "    JOIN hkidb.tag t ON bit.tid = t.tid AND bit.tid IN (:tids) " +
                        "    WHERE t.uid = :uid " +
                        "    GROUP BY bit.aid " +
                        "    HAVING COUNT(DISTINCT bit.tid) = :tid_length);",
                src,
                (rs, rowNum) -> BacktestHistory.builder()
                        .uid(rs.getInt("uid"))
                        .aid(rs.getInt("aid"))
                        .param(rs.getString("param"))
                        .plratio(rs.getDouble("plratio"))
                        .build()
        );
    }
}
