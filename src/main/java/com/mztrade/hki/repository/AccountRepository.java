package com.mztrade.hki.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.entity.AccountHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
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
public class AccountRepository {
    private final NamedParameterJdbcTemplate template;
    private final ObjectMapper objectMapper;

    @Autowired
    public AccountRepository(NamedParameterJdbcTemplate template, ObjectMapper objectMapper) {
        this.template = template;
        this.objectMapper = objectMapper;
    }

    public Integer createBacktestAccount(Integer uid) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("uid", uid, Types.INTEGER);
        this.template.update(
                "INSERT INTO hkidb.account (uid) VALUES (:uid)",
                src,
                keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    public Integer createGameAccount(Integer uid) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("uid", uid, Types.INTEGER)
                .addValue("type", "GAME", Types.VARCHAR);
        this.template.update(
                "INSERT INTO hkidb.account (uid, type) VALUES (:uid, :type)",
                src,
                keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    public Boolean deleteAccount(Integer aid) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("aid", aid, Types.INTEGER);
        try {
            this.template.update(
                    "DELETE FROM hkidb.account WHERE (aid = :aid)",
                    src,
                    keyHolder);
            return true;
        } catch (DataAccessException e) {
            return false;
        }
    }

    public Long getBalance(Integer aid) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("aid", aid, Types.INTEGER);
        return this.template.queryForObject(
                "SELECT a.balance FROM hkidb.account a WHERE a.aid = :aid",
                src,
                Long.class
        );
    }

    public void updateBalance(Integer aid, Long balance) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("aid", aid, Types.INTEGER)
                .addValue("balance", balance, Types.BIGINT);
        this.template.update(
                "UPDATE hkidb.account a SET a.balance = :balance WHERE a.aid = :aid",
                src
        );
    }

    public List<Integer> getAll(int uid) {
        List<Integer> aids = new ArrayList<>();
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("uid", uid, Types.INTEGER);
        return this.template.queryForList(
                "SELECT a.aid FROM hkidb.account a WHERE a.uid = :uid",
                src,
                Integer.class
        );
    }

    public Boolean createAccountHistory(Integer aid, LocalDateTime date, long balance) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("aid", aid, Types.INTEGER)
                .addValue("date", date, Types.TIMESTAMP)
                .addValue("balance", balance, Types.BIGINT);
        try {
            this.template.update(
                    "INSERT INTO hkidb.account_history (aid, date, balance) VALUES (:aid, :date, :balance)",
                    src);
        } catch (DataAccessException e) {
            return false;
        }
        return true;
    }

    public List<AccountHistory> getAccountHistory(int aid) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("aid", aid, Types.INTEGER);
        try {
            return this.template.query(
                    "SELECT aid, date, balance FROM hkidb.account_history WHERE aid = :aid",
                    src,
                    (rs, rowNum) -> AccountHistory.builder()
                            .aid(rs.getInt("aid"))
                            .date(rs.getTimestamp("date").toLocalDateTime())
                            .balance(rs.getLong("balance"))
                            .build()
            );
        } catch (DataAccessException e) {
            return null;
        }
    }




}
