package com.mztrade.hki.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.entity.Position;
import com.mztrade.hki.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.Objects;
import java.util.Optional;

@Component
public class UserRepository {
    private final NamedParameterJdbcTemplate template;
    private final ObjectMapper objectMapper;

    @Autowired
    public UserRepository(NamedParameterJdbcTemplate template, ObjectMapper objectMapper) {
        this.template = template;
        this.objectMapper = objectMapper;
    }

    public int createUser(String name, String password) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("name", name, Types.VARCHAR)
                .addValue("password", password, Types.VARCHAR);
        this.template.update(
                "INSERT INTO hkidb.user (name, password) VALUES (:name, :password)",
                src,
                keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    public Optional<User> getUser(String name) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("name", name, Types.VARCHAR);
        return this.template.queryForObject(
                "SELECT u.uid, u.name, u.password FROM hkidb.user u WHERE u.name = :name",
                src,
                (rs, rowNum) ->
                        Optional.of(
                                new User()
                                        .setUid(rs.getInt("u.uid"))
                                        .setName(rs.getString("u.name"))
                                        .setPassword(rs.getString("u.password"))
                        )
        );
    }
}
