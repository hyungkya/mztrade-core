package com.mztrade.hki.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.entity.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Types;

import java.util.List;
import java.util.Optional;

@Component
public class PositionRepositoryImpl {
    private final NamedParameterJdbcTemplate template;
    private final ObjectMapper objectMapper;

    @Autowired
    public PositionRepositoryImpl(NamedParameterJdbcTemplate template, ObjectMapper objectMapper) {
        this.template = template;
        this.objectMapper = objectMapper;
    }

    public Optional<Position> getPositionByTicker(Integer aid, String ticker) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("aid", aid, Types.INTEGER)
                .addValue("ticker", ticker, Types.VARCHAR);
        try {
            return this.template.queryForObject(
                    "SELECT p.qty, p.avg_entry_price FROM hkidb.position p "
                            + "WHERE (p.aid = :aid AND p.ticker = :ticker)",
                    src,
                    (rs, rowNum) ->
                            Optional.of(
                                    new Position().toBuilder()
                                    .aid(aid)
                                    .ticker(ticker)
                                    .qty(rs.getInt("p.qty"))
                                    .avgEntryPrice(rs.getBigDecimal("p.avg_entry_price")).build()
                            )
            );
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Position> getAllPositions(Integer aid) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("aid", aid, Types.INTEGER);
        return this.template.query(
                "SELECT * FROM hkidb.position "
                        + "WHERE (aid = :aid)",
                src,
                (rs, rowNum) ->
                        new Position()
                                .toBuilder()
                                .aid(aid)
                                .ticker(rs.getString("ticker"))
                                .qty(rs.getInt("qty"))
                                .avgEntryPrice(rs.getBigDecimal("avg_entry_price")).build()
        );
    }

    public void createPosition(Position position) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("aid", position.getAid(), Types.INTEGER)
                .addValue("ticker", position.getTicker(), Types.VARCHAR)
                .addValue("qty", position.getQty(), Types.INTEGER)
                .addValue("avg_entry_price", position.getAvgEntryPrice(), Types.DECIMAL);
        this.template.update(
                "INSERT INTO hkidb.position (aid, ticker, qty, avg_entry_price) "
                        + "VALUES (:aid, :ticker, :qty, :avg_entry_price) ",
                src);
    }

    public void deletePosition(Integer aid, String ticker) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("aid", aid, Types.INTEGER)
                .addValue("ticker", ticker, Types.VARCHAR);
        this.template.update(
                "DELETE FROM hkidb.position "
                        + "WHERE (aid = :aid) AND (ticker = :ticker) ",
                src);
    }

    public void updatePosition(Position position) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("aid", position.getAid(), Types.INTEGER)
                .addValue("ticker", position.getTicker(), Types.VARCHAR)
                .addValue("qty", position.getQty(), Types.INTEGER)
                .addValue("avg_entry_price", position.getAvgEntryPrice(), Types.DECIMAL);
        this.template.update(
                "UPDATE hkidb.position SET "
                        + "qty = :qty, avg_entry_price = :avg_entry_price "
                        + "WHERE (aid = :aid) AND (ticker = :ticker)",
                src);
    }
}
