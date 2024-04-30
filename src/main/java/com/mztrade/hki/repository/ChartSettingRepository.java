package com.mztrade.hki.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.entity.ChartSetting;
import java.sql.Types;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChartSettingRepository {
    private final NamedParameterJdbcTemplate template;
    private final ObjectMapper objectMapper;

    @Autowired
    public ChartSettingRepository(NamedParameterJdbcTemplate template, ObjectMapper objectMapper) {
        this.template = template;
        this.objectMapper = objectMapper;
    }

    public boolean save(ChartSetting chartSetting) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("uid", chartSetting.getUid(), Types.INTEGER)
                .addValue("indicator", chartSetting.getIndicator(), Types.VARCHAR);
        try {
            this.template.update(
                    "UPDATE hkidb.chart_setting SET indicator = :indicator WHERE uid = :uid",
                    src);
        } catch (DataAccessException e) {
            return false;
        }
        return true;
    }

    public ChartSetting get(int aid) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("uid", aid, Types.INTEGER);
        try {
            return this.template.queryForObject(
                    "SELECT uid, indicator FROM hkidb.chart_setting WHERE uid = :uid",
                    src,
                    (rs, rowNum) -> ChartSetting.builder()
                            .uid(rs.getInt("uid"))
                            .indicator(rs.getString("indicator"))
                            .build()
            );
        } catch (DataAccessException e) {
            return null;
        }
    }
}
