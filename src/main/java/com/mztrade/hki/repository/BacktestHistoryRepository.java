package com.mztrade.hki.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.entity.backtest.BacktestHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.List;

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
                .addValue("param", backtestHistory.getParam(), Types.VARCHAR);
        try {
            this.template.update(
                    "INSERT INTO hkidb.backtest_history (uid, aid, param) VALUES (:uid, :aid, :param)",
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
                    "SELECT b.aid, b.uid, b.param FROM hkidb.backtest_history b WHERE b.aid = :aid",
                    src,
                    (rs, rowNum) -> new BacktestHistory()
                            .setUid(rs.getInt("b.uid"))
                            .setAid(rs.getInt("b.aid"))
                            .setParam(rs.getString("b.param"))
            );
        } catch (DataAccessException e) {
            return null;
        }
    }

    public List<BacktestHistory> searchByTitle(int uid, String title) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("uid", uid, Types.INTEGER)
                .addValue("title", "%" + title + "%", Types.VARCHAR);

        try {
            return this.template.query(
                    "SELECT b.aid, b.uid, b.param " +
                            "FROM hkidb.backtest_history b " +
                            "WHERE b.uid = :uid AND JSON_EXTRACT(b.param, '$.title') LIKE :title",
                    src,
                    (rs, rowNum) -> new BacktestHistory()
                            .setUid(rs.getInt("b.uid"))
                            .setAid(rs.getInt("b.aid"))
                            .setParam(rs.getString("b.param"))
            );
        } catch (DataAccessException e) {
            System.out.println(e);
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
}
