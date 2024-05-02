/*
package com.mztrade.hki.service;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mztrade.hki.dto.LoginRequestDto;
import com.mztrade.hki.dto.TokenDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthServiceTest {

    @Autowired
    private MockMvc mockMvc;

    // 객체 <-> json
    @Autowired
    private ObjectMapper objectMapper;

    private TokenDto tokenDto;

    @Test
    @DisplayName("토큰 생성 테스트")
    void createToken() throws Exception {

        // given
        LoginRequestDto loginRequestDto = new LoginRequestDto("test", "test");

        // When
        ResultActions resultActions = mockMvc.perform(
                                        MockMvcRequestBuilders
                                                .post("/auth/issue")
                                                .contentType("application/json")
                                                .content(
                                                        objectMapper.writeValueAsString(loginRequestDto)
                                                )


        );

        tokenDto = objectMapper.readValue(
                resultActions.andReturn().getResponse().getContentAsString(), TokenDto.class);

        System.out.println("tokenDto.toString() = " + tokenDto.toString());
        // Then
        resultActions.andExpect(status().isOk());

    }

    @Test
    @DisplayName("토큰 재발급 테스트")
    void reissueToken() throws Exception {

    // given
    TokenDto reissueTokenDto = tokenDto;

    // When
    ResultActions resultActions = mockMvc.perform(
                                    MockMvcRequestBuilders
                                            .post("/auth/reissue")
                                            .contentType("application/json")
                                            .content(
                                                    objectMapper.writeValueAsString(reissueTokenDto)
                                            ));


    // Then : 토큰 발급 후 바로 요청하면 재발급이 되지 않음
    resultActions.andExpect(status().isBadRequest());

    }


}
*/
