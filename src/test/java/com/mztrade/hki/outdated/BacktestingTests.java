package com.mztrade.hki.outdated;

import com.mztrade.hki.Util;
import com.mztrade.hki.entity.backtest.Condition;
import com.mztrade.hki.entity.backtest.Indicator;
import com.mztrade.hki.service.BacktestService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Sql(scripts = {"classpath:schema.sql", "classpath:db/stock_info_1205.sql", "classpath:db/stock_price_1205.sql"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class BacktestingTests {
    @Autowired
    private BacktestService backtestService;

}
