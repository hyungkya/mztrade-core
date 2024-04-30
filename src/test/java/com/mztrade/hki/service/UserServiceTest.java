package com.mztrade.hki.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("패스 암호화 테스트")
    void passwordEncoderTest() {
        // given
        String password = "1234";

        // when
        String encodedPassword = passwordEncoder.encode(password);

        // then
        System.out.println("encodedPassword = " + encodedPassword);
        System.out.println("encodedPassword.length() = " + encodedPassword.length());
    }

}