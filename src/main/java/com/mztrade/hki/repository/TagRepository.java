package com.mztrade.hki.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.entity.Tag;
import com.mztrade.hki.entity.TagCategory;
import com.mztrade.hki.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class TagRepository {
    private final NamedParameterJdbcTemplate template;
    private final ObjectMapper objectMapper;

    @Autowired
    public TagRepository(NamedParameterJdbcTemplate template, ObjectMapper objectMapper) {
        this.template = template;
        this.objectMapper = objectMapper;
    }

    public int createTag(int uid, String name, String color, TagCategory category) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("uid", uid, Types.INTEGER)
                .addValue("name", name, Types.VARCHAR)
                .addValue("color", color, Types.VARCHAR)
                .addValue("category", category.id(), Types.INTEGER);
        this.template.update(
                "INSERT INTO hkidb.tag (uid, tname, tcolor, category) VALUES (:uid, :name, :color, :category)",
                src,
                keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    public List<Tag> findByCategory(int uid, TagCategory category) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("uid", uid, Types.INTEGER)
                .addValue("category", category.id(), Types.INTEGER);
        return this.template.query(
                "SELECT t.uid, t.tid, t.tname, t.tcolor, t.category FROM hkidb.tag t WHERE t.uid = :uid AND t.category = :category",
                src,
                (rs, rowNum) ->
                        Tag.builder()
                                .uid(rs.getInt("t.uid"))
                                .tid(rs.getInt("t.tid"))
                                .tname(rs.getString("t.tname"))
                                .tcolor(rs.getString("t.tcolor"))
                                .category(rs.getInt("t.category"))
                                .build()
        );
    }

    public boolean deleteById(int tid) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("tid", tid, Types.INTEGER);
        int deletedRows = this.template.update(
                "DELETE FROM hkidb.tag t WHERE t.tid = :tid",
                src
        );
        if (deletedRows > 1) {
            throw new RuntimeException();
        }
        return deletedRows == 1 ? true : false;
    }
}
