/*
package com.mztrade.hki.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.entity.backtest.BacktestHistory;
import com.mztrade.hki.entity.backtest.BacktestParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Component
public class  BacktestHistoryRepositoryImpl {
    private final NamedParameterJdbcTemplate template;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public BacktestHistoryRepositoryImpl(NamedParameterJdbcTemplate template, ObjectMapper objectMapper, UserRepository userRepository, AccountRepository accountRepository) {
        this.template = template;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    public boolean create(BacktestHistory backtestResult) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("uid", backtestResult.getUid(), Types.INTEGER)
                .addValue("aid", backtestResult.getAid(), Types.INTEGER)
                .addValue("param", backtestResult.getParam(), Types.VARCHAR)
                .addValue("plratio", backtestResult.getPlratio(), Types.DOUBLE);
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
    public List<BacktestHistory> getBacktestTop5(int uid) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("uid", uid, Types.INTEGER);
        try {
            return this.template.query(
                    "select * from backtest_history where uid = :uid order by plratio desc limit 5",
                    src,
                    (rs, rowNum) -> BacktestHistory.builder()
                            .uid(rs.getInt("uid"))
                            .aid(rs.getInt("aid"))
                            .param(rs.getString("param"))
                            .plratio(rs.getDouble("plratio"))
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

    public Optional<BacktestParameter> getBacktestParameter(int aid) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("aid", aid, Types.INTEGER);
        Optional<BacktestParameter> backtestParameter;
        backtestParameter = Optional.of(
                this.template.queryForObject(
                "SELECT b.param FROM hkidb.backtest_history b WHERE b.aid = :aid",
                src,
                (rs, rowNum) -> {
                    try {
                        return objectMapper.readValue(rs.getString("b.param"), BacktestParameter.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
        ));
        return backtestParameter;
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
                            .user(userRepository.getReferenceById(rs.getInt("b.uid")))
                            .account(accountRepository.getReferenceById(rs.getInt("b.aid")))
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
}
*/
