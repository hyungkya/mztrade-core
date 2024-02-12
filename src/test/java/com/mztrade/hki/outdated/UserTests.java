package com.mztrade.hki.outdated;

import static org.junit.jupiter.api.Assertions.*;

import com.mztrade.hki.dto.UserDto;
import com.mztrade.hki.service.UserService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
// @Sql(scripts = {"classpath:db/schema.sql", "classpath:db/stock_info_1205.sql", "classpath:db/stock_price_1205.sql"})
public class UserTests {
    @Autowired
    private UserService userService;

    @Test
    void saveUserTest() throws Exception {
        UserDto userDto = new UserDto("testUser", "testUser");
        int uid = userService.saveUser(userDto);
        Assertions.assertThat(uid).isEqualTo(6);
    }

    @Test
    void loginTest() throws Exception {
        UserDto userDto = new UserDto("testUser", "testUser");
        assertDoesNotThrow(()->userService.login(userDto.getName(), userDto.getPassword()));
    }

    @Test
    void loginExceptionTest1() throws Exception {
        UserDto userDto = new UserDto("js", "js");
        assertThrows(Exception.class, ()->userService.login(userDto.getName(), userDto.getPassword()));
    }

    @Test
    void loginExceptionTest2() throws Exception {
        UserDto userDto = new UserDto("js", "js");
        try {
            userService.login(userDto.getName(),userDto.getPassword());
        }catch (Exception e) {
            assertEquals("유효한 회원ID가 아닙니다.", e.getMessage());
        }
    }

    @Test
    void loginExceptionTest3() throws Exception {
        UserDto userDto = new UserDto("ADMIN", "js");
        try {
            userService.login(userDto.getName(),userDto.getPassword());
        }catch (Exception e) {
            assertEquals("유효한 회원 패스워드가 아닙니다.", e.getMessage());
        }
    }
}
