/*
package com.mztrade.hki.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.entity.StockInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.List;

@Component
public class StockInfoRepositoryImpl {
    private final NamedParameterJdbcTemplate template;
    private final ObjectMapper objectMapper;

    @Autowired
    public StockInfoRepositoryImpl(NamedParameterJdbcTemplate template, ObjectMapper objectMapper) {
        this.template = template;
        this.objectMapper = objectMapper;
    }
    //TODO::페이징 기능 추가하기
    public List<StockInfo> getAll() {
        return this.template.query(
                "SELECT * FROM hkidb.stock_info ",
                (rs, rowNum) -> StockInfo.builder()
                        .ticker(rs.getString("ticker"))
                        .name(rs.getString("name"))
                        .listedDate(rs.getDate("listed_date").toLocalDate())
                        .marketCapital(rs.getInt("market_capital"))
                        .build());
    }

    public StockInfo findByTicker(String ticker) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("ticker", ticker, Types.VARCHAR);

        return this.template.queryForObject(
                "SELECT * FROM hkidb.stock_info si WHERE si.ticker = :ticker",
                src,
                (rs, rowNum) -> StockInfo.builder()
                        .ticker(rs.getString("ticker"))
                        .name(rs.getString("name"))
                        .listedDate(rs.getDate("listed_date").toLocalDate())
                        .marketCapital(rs.getInt("market_capital"))
                        .build());
    }

    public List<StockInfo> findByName(String name) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("name", "%" + name + "%", Types.VARCHAR);

        return this.template.query(
                "SELECT * FROM hkidb.stock_info si WHERE si.name LIKE :name",
                src,
                (rs, rowNum) -> StockInfo.builder()
                        .ticker(rs.getString("ticker"))
                        .name(rs.getString("name"))
                        .listedDate(rs.getDate("listed_date").toLocalDate())
                        .marketCapital(rs.getInt("market_capital"))
                        .build());
    }
}
*/
