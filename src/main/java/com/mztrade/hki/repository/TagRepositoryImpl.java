package com.mztrade.hki.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.entity.StockInfo;
import com.mztrade.hki.entity.Tag;
import com.mztrade.hki.entity.TagCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;
import java.util.Objects;

@Repository
public class TagRepositoryImpl {
    private final NamedParameterJdbcTemplate template;
    private final ObjectMapper objectMapper;

    @Autowired
    public TagRepositoryImpl(NamedParameterJdbcTemplate template, ObjectMapper objectMapper) {
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

    public boolean updateTag(int tid, String name, String color) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("tid", tid, Types.INTEGER)
                .addValue("name", name, Types.VARCHAR)
                .addValue("color", color, Types.VARCHAR);
        int updateRow = this.template.update(
                "UPDATE hkidb.tag SET tname = :name, tcolor = :color WHERE tid = :tid",
                src);
        return updateRow == 1;
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

    public List<Tag> findByAid(int uid, int aid) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("uid", uid, Types.INTEGER)
                .addValue("aid", aid, Types.INTEGER);
        return this.template.query(
                "SELECT t.uid, t.tid, t.tname, t.tcolor, t.category FROM hkidb.tag t JOIN hkidb.backtest_history_tag bht ON t.tid = bht.tid WHERE t.uid = :uid AND bht.aid = :aid",
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

    public List<Tag> findByTicker(Integer uid, String ticker) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("uid", uid, Types.INTEGER)
                .addValue("ticker", ticker, Types.VARCHAR);
        return this.template.query(
                "SELECT t.uid, t.tid, t.tname, t.tcolor, t.category FROM hkidb.tag t JOIN hkidb.stock_info_tag sit ON t.tid = sit.tid WHERE t.uid = :uid AND sit.ticker LIKE :ticker",
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

    public boolean createBacktestHistoryTagLink(int tid, int aid) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("tid", tid, Types.INTEGER)
                .addValue("aid", aid, Types.INTEGER);
        int affectedRows = this.template.update(
                "INSERT INTO hkidb.backtest_history_tag (tid, aid) VALUES (:tid, :aid)",
                src);
        return affectedRows == 1 ? true : false;
    }

    public boolean deleteBacktestHistoryTagLink(int tid, int aid) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("tid", tid, Types.INTEGER)
                .addValue("aid", aid, Types.INTEGER);
        int affectedRows = this.template.update(
                "DELETE FROM hkidb.backtest_history_tag bht WHERE bht.tid = :tid AND bht.aid = :aid",
                src);
        return affectedRows == 1 ? true : false;
    }

    public boolean createStockInfoTagLink(int tid, String ticker) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("tid", tid, Types.INTEGER)
                .addValue("ticker", ticker, Types.VARCHAR);
        int affectedRows = this.template.update(
                "INSERT INTO hkidb.stock_info_tag (tid, ticker) VALUES (:tid, :ticker)",
                src);
        return affectedRows == 1 ? true : false;
    }

    public boolean deleteStockInfoTagLink(int tid, String ticker) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("tid", tid, Types.INTEGER)
                .addValue("ticker", ticker, Types.VARCHAR);
        int affectedRows = this.template.update(
                "DELETE FROM hkidb.stock_info_tag sit WHERE sit.tid = :tid AND sit.ticker LIKE :ticker",
                src);
        return affectedRows == 1 ? true : false;
    }

    public List<StockInfo> findStockInfoByNameAndTags(int uid, String name, List<Integer> tids) {
        MapSqlParameterSource src = new MapSqlParameterSource()
                .addValue("uid", uid, Types.INTEGER)
                .addValue("name", name + "%", Types.VARCHAR)
                .addValue("tids", tids)
                .addValue("tid_length", tids.size(), Types.INTEGER);
        return this.template.query(
                "SELECT si.* " +
                        "FROM hkidb.stock_info si " +
                        "WHERE si.name LIKE :name AND si.ticker IN ( " +
                        "    SELECT sit.ticker " +
                        "    FROM hkidb.stock_info_tag sit " +
                        "    JOIN hkidb.tag t ON sit.tid = t.tid AND sit.tid IN (:tids) " +
                        "    WHERE t.uid = :uid " +
                        "    GROUP BY sit.ticker " +
                        "    HAVING COUNT(DISTINCT sit.tid) = :tid_length);",
                src,
                (rs, rowNum) ->
                        StockInfo.builder()
                                .ticker(rs.getString("ticker"))
                                .name(rs.getString("name"))
                                .listedDate(rs.getDate("listed_date").toLocalDate())
                                .marketCapital(rs.getInt("market_capital"))
                                .build()
        );
    }


}
