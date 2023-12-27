package com.mztrade.hki.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.Types;
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

    public Integer createAccount(Integer uid) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("uid", uid, Types.INTEGER);
        this.template.update(
                "INSERT INTO hkidb.account (uid) VALUES (:uid)",
                src,
                keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
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
}
