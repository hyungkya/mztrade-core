package com.mztrade.hki.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.entity.Bar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class StockPriceRepositoryImpl {
    private final NamedParameterJdbcTemplate template;
    private final ObjectMapper objectMapper;

    @Autowired
    public StockPriceRepositoryImpl(NamedParameterJdbcTemplate template, ObjectMapper objectMapper) {
        this.template = template;
        this.objectMapper = objectMapper;
    }

    public void add(Bar bar) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("ticker", bar.getTicker(), Types.VARCHAR)
                .addValue("date", bar.getDate(), Types.TIMESTAMP)
                .addValue("open", bar.getOpen(), Types.INTEGER)
                .addValue("high", bar.getHigh(), Types.INTEGER)
                .addValue("low", bar.getLow(), Types.INTEGER)
                .addValue("close", bar.getClose(), Types.INTEGER)
                .addValue("volume", bar.getVolume(), Types.BIGINT);

        this.template.update(
                "INSERT INTO hkidb.stock_price (ticker, date, open, high, low, close, volume) "
                        + "VALUES (:ticker, :date, :open, :high, :low, :close, :volume)"
                        + "ON DUPLICATE KEY UPDATE open = :open, high = :high, low = :low, close = :close, volume = :volume",
                src);
    }

    public Bar findByDate(String ticker, LocalDateTime date) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("ticker", ticker, Types.VARCHAR)
                .addValue("date", date, Types.TIMESTAMP);

        return this.template.queryForObject(
                "SELECT * FROM hkidb.stock_price "
                        + "WHERE (ticker = :ticker AND date = :date)",
                src,
                (rs, rowNum) -> new Bar().toBuilder()
                        .ticker(rs.getString("ticker"))
                        .date(rs.getTimestamp("date").toLocalDateTime())
                        .open(rs.getInt("open"))
                        .high(rs.getInt("high"))
                        .low(rs.getInt("low"))
                        .close(rs.getInt("close"))
                        .volume(rs.getLong("volume"))).build();
    }

    public List<Bar> findByDate(String ticker, LocalDateTime startDate, LocalDateTime endDate) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("ticker", ticker, Types.VARCHAR)
                .addValue("start_date", startDate, Types.TIMESTAMP)
                .addValue("end_date", endDate, Types.TIMESTAMP);

        return this.template.query(
                "SELECT * FROM hkidb.stock_price "
                        + "WHERE (ticker = :ticker AND (date BETWEEN :start_date AND :end_date))",
                src,
                (rs, rowNum) -> new Bar().toBuilder()
                        .ticker(rs.getString("ticker"))
                        .date(rs.getTimestamp("date").toLocalDateTime())
                        .open(rs.getInt("open"))
                        .high(rs.getInt("high"))
                        .low(rs.getInt("low"))
                        .close(rs.getInt("close"))
                        .volume(rs.getLong("volume")).build());
    }

    public List<Bar> findByTicker(String ticker) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("ticker", ticker, Types.VARCHAR);

        return this.template.query(
                "SELECT * FROM hkidb.stock_price "
                        + "WHERE (ticker = :ticker)",
                src,
                (rs, rowNum) -> new Bar().toBuilder()
                        .ticker(rs.getString("ticker"))
                        .date(rs.getTimestamp("date").toLocalDateTime())
                        .open(rs.getInt("open"))
                        .high(rs.getInt("high"))
                        .low(rs.getInt("low"))
                        .close(rs.getInt("close"))
                        .volume(rs.getLong("volume")).build());
    }
}
