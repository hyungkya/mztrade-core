/*
package com.mztrade.hki.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.entity.Order;
import com.mztrade.hki.entity.OrderType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.List;
import java.util.Objects;

@Component
public class OrderHistoryRepositoryImpl {
    private final NamedParameterJdbcTemplate template;
    private final ObjectMapper objectMapper;

    @Autowired
    public OrderHistoryRepositoryImpl(NamedParameterJdbcTemplate template, ObjectMapper objectMapper) {
        this.template = template;
        this.objectMapper = objectMapper;
    }

    public Integer createOrderHistory(Order order) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("aid", order.getAid(), Types.INTEGER)
                .addValue("otid", order.getOtid(), Types.INTEGER)
                .addValue("filled_time", order.getFilledTime(), Types.TIMESTAMP)
                .addValue("ticker", order.getTicker(), Types.VARCHAR)
                .addValue("qty", order.getQty(), Types.INTEGER)
                .addValue("price", order.getPrice(), Types.INTEGER)
                .addValue("avg_entry_price", order.getAvgEntryPrice(), Types.INTEGER);
        this.template.update(
                "INSERT INTO hkidb.order_history (aid, otid, filled_time, ticker, qty, price, avg_entry_price) "
                        + "VALUES (:aid, :otid, :filled_time, :ticker, :qty, :price, :avg_entry_price)",
                src,
                keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).intValue();
    }

    public List<Order> get(Integer aid) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("aid", aid, Types.INTEGER);
        String sql = "SELECT * FROM hkidb.order_history "
                + "WHERE (aid = :aid) ";
        return this.template.query(
                sql,
                src,
                (rs, rowNum) -> Order.builder()
                        .aid(aid)
                        .avgEntryPrice(rs.getBigDecimal("avg_entry_price"))
                        .filledTime(rs.getTimestamp("filled_time").toLocalDateTime())
                        .ticker(rs.getString("ticker"))
                        .qty(rs.getInt("qty"))
                        .price(rs.getInt("price"))
                        .oid(rs.getInt("oid"))
                        .otid(rs.getInt("otid"))
                        .build());
    }

    public List<Order> get(Integer aid, Integer option) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("aid", aid, Types.INTEGER);

        String sql = "SELECT * FROM hkidb.order_history "
                + "WHERE (aid = :aid) ";

        if (option == OrderType.BUY.id()) {
            sql += String.format("AND (otid = %d)", OrderType.BUY.id());
        } else if (option == OrderType.SELL.id()) {
            sql += String.format("AND (otid = %d)", OrderType.SELL.id());
        }

        return this.template.query(
                sql,
                src,
                (rs, rowNum) -> Order.builder()
                        .aid(aid)
                        .avgEntryPrice(rs.getBigDecimal("avg_entry_price"))
                        .filledTime(rs.getTimestamp("filled_time").toLocalDateTime())
                        .ticker(rs.getString("ticker"))
                        .qty(rs.getInt("qty"))
                        .price(rs.getInt("price"))
                        .oid(rs.getInt("oid"))
                        .otid(rs.getInt("otid"))
                        .build());
    }

    public List<Order> get(Integer aid, String ticker) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("aid", aid, Types.INTEGER)
                .addValue("ticker", ticker, Types.VARCHAR);
        String sql = "SELECT * FROM hkidb.order_history "
                + "WHERE (aid = :aid) AND (ticker LIKE :ticker) ";
        return this.template.query(
                sql,
                src,
                (rs, rowNum) -> Order.builder()
                        .aid(aid)
                        .avgEntryPrice(rs.getBigDecimal("avg_entry_price"))
                        .filledTime(rs.getTimestamp("filled_time").toLocalDateTime())
                        .ticker(rs.getString("ticker"))
                        .qty(rs.getInt("qty"))
                        .price(rs.getInt("price"))
                        .oid(rs.getInt("oid"))
                        .otid(rs.getInt("otid"))
                        .build());
    }

    public List<Order> get(Integer aid, String ticker, Integer option) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("aid", aid, Types.INTEGER)
                .addValue("ticker", ticker, Types.VARCHAR);
        String sql = "SELECT * FROM hkidb.order_history "
                + "WHERE (aid = :aid) AND (ticker LIKE :ticker) ";

        if (option == OrderType.BUY.id()) {
            sql += String.format("AND (otid = %d)", OrderType.BUY.id());
        } else if (option == OrderType.SELL.id()) {
            sql += String.format("AND (otid = %d)", OrderType.SELL.id());
        }

        return this.template.query(
                sql,
                src,
                (rs, rowNum) -> Order.builder()
                        .aid(aid)
                        .avgEntryPrice(rs.getBigDecimal("avg_entry_price"))
                        .filledTime(rs.getTimestamp("filled_time").toLocalDateTime())
                        .ticker(rs.getString("ticker"))
                        .qty(rs.getInt("qty"))
                        .price(rs.getInt("price"))
                        .oid(rs.getInt("oid"))
                        .otid(rs.getInt("otid"))
                        .build());
    }
}
*/
