package com.mztrade.hki.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.entity.Order;
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
public class OrderHistoryRepository {
    private final NamedParameterJdbcTemplate template;
    private final ObjectMapper objectMapper;

    @Autowired
    public OrderHistoryRepository(NamedParameterJdbcTemplate template, ObjectMapper objectMapper) {
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

    public List<Order> findByAid(Integer aid) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("aid", aid, Types.INTEGER);
        return this.template.query(
                "SELECT * FROM hkidb.order_history "
                        + "WHERE (aid = :aid)",
                src,
                (rs, rowNum) -> Order.builder()
                        .aid(aid)
                        .avgEntryPrice(rs.getBigDecimal("avg_entry_price"))
                        .filledTime(rs.getTimestamp("filled_time").toInstant())
                        .ticker(rs.getString("ticker"))
                        .qty(rs.getInt("qty"))
                        .price(rs.getInt("price"))
                        .oid(rs.getInt("oid"))
                        .otid(rs.getInt("otid"))
                        .build());
    }
}
