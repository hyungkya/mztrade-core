package com.mztrade.hki.outdated;

import com.mztrade.hki.service.UserService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@Sql(scripts = {"classpath:db/schema.sql", "classpath:db/stock_info_1205.sql", "classpath:db/stock_price_1205.sql"})
public class UserTests {
    @Autowired
    private UserService userService;

    @Test
    void simpleUserTest() {
        int uid = userService.createUser("Hyungkyu", "Hyungkyu");
        Assertions.assertThat(uid).isEqualTo(2);
        boolean didLogin = userService.login("Hyungkyu", "Hyungkyu");
        Assertions.assertThat(didLogin).isEqualTo(true);
    }

    @Test
    void userTest() {
        int uid = userService.createUser("Hyungkyu", "Hyungkyu");
        Assertions.assertThat(uid).isEqualTo(2);
        boolean didLogin = userService.login("Minjun", "Hyungkyu");
        Assertions.assertThat(didLogin).isEqualTo(false);
    }
}
