package com.mztrade.hki;

import com.mztrade.hki.controller.StockController;
import com.mztrade.hki.controller.UserController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class StockTests {
    @Autowired
    StockController stockController;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getStockWithNoQuery() throws Exception {
        //조건 설정 없이 전부 다 요청하는 경우
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/stock"))
                .andExpect(status().isOk());
    }

    @Test
    public void getStockWithName() throws Exception {
        //특정 제목 조건으로 요청하는 경우
        String expectByName = "$.[?(@.name == '%s')]";

        mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/stock")
                        .queryParam("name", "기아")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByName, "기아").exists());
    }

    @Test
    public void getStockWithTags() throws Exception {
        //특정 태그 조건으로 요청하는 경우
        String expectByName = "$.[?(@.name == '%s')]";

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .get("/stock")
                                .queryParam("uid", "1")
                                .queryParam("tids", "3")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByName, "기아").exists())
                .andExpect(jsonPath(expectByName, "SK하이닉스").exists());
    }

    @Test
    public void getStockWithNameAndTags() throws Exception {
        //특정 태그 조건으로 요청하는 경우
        String expectByName = "$.[?(@.name == '%s')]";

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .get("/stock")
                                .queryParam("uid", "1")
                                .queryParam("tids", "3")
                                .queryParam("name", "SK")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByName, "SK하이닉스").exists());
    }

    @Test
    public void getStockWithTagsBadRequest() throws Exception {
        //uid 없이 특정 태그 조건으로 요청하는 경우 -> BAD_REQUEST
        mockMvc.perform(
                        MockMvcRequestBuilders
                                .get("/stock")
                                .queryParam("tids", "3")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getStockPrice() throws Exception {
        //단순 주가 요청
        String ticker = "000270";
        String expectByDate = "$.[?(@.date == '%s')]";
        String expectByOpen = "$.[?(@.open == '%s')]";

        mockMvc.perform(
                MockMvcRequestBuilders
                        .get(String.format("/stock/%s/price", ticker))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByDate, "2013-01-02T09:00:00").exists())
                .andExpect(jsonPath(expectByOpen, 57000).exists())
                .andExpect(jsonPath(expectByDate, "2022-12-29T09:00:00").exists())
                .andExpect(jsonPath(expectByOpen, 60500).exists());
    }
}
