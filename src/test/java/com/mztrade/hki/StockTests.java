package com.mztrade.hki;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mztrade.hki.controller.StockController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;


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

    @Test
    public void getStockInfo() throws Exception {
        //단순 주식 상세 정보 요청
        String ticker = "000270";
        String expectByListedDate = "$.[?(@.listed_date == '%s')]";
        String expectByListedMarket = "$.[?(@.listed_market == '%s')]";

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .get(String.format("/stock/%s/info", ticker))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByListedDate, "1973-07-21").exists())
                .andExpect(jsonPath(expectByListedMarket, "코스피").exists());
    }

    @Test
    public void getStockIndicator() throws Exception {
        //특정날의 주가에 대한 보조지표 요청
        String ticker = "000270";
        String startDate = "20150105";
        String endDate = "20150331";
        String type = "RSI";
        String param = "14";

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .get(String.format("/stock/%s/indicator", ticker))
                                .queryParam("startDate", startDate)
                                .queryParam("type", type)
                                .queryParam("param", param)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("34.883720930232556"));
    }

    @Test
    public void getStockIndicatorRange() throws Exception {
        //특정 기간의 주가에 대한 보조지표 요청
        String ticker = "000270";
        String startDate = "20150105";
        String endDate = "20150331";
        String type = "RSI";
        String param = "14";

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .get(String.format("/stock/%s/indicator", ticker))
                                .queryParam("startDate", startDate)
                                .queryParam("endDate", endDate)
                                .queryParam("type", type)
                                .queryParam("param", param)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[?(@.2015-02-23T09:00 == '%s')]", 33.65384615384615).exists());
    }

    @Test
    public void getStockFinancialInfo() throws Exception {
        //주식 재무 상세 정보 요청
        String ticker = "000270";
        String expectByCapital = "$.[?(@.capital == '%s')]";

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .get(String.format("/stock/%s/financial-info", ticker))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByCapital, 21393).exists());
    }
}
