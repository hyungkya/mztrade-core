package com.mztrade.hki;

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
public class UserTests {
    @Autowired
    UserController userController;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getUser() throws Exception {
        String expectByName = "$.[?(@.name == '%s')]";

        String firebaseUid = "xYt1jzToaccfhDVKpmZg5Wu2Jqs2";
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/user").queryParam("firebaseUid", firebaseUid))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByName, "ADMIN").exists());
    }

    @Test
    public void postUser() throws Exception {
        String firebaseUid = "I_AM_FIREBASE";
        String name = "NAME";
        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post("/user")
                                .queryParam("firebaseUid", firebaseUid)
                                .queryParam("name", name))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @Test
    public void deleteUser() throws Exception {
        String firebaseUid = "xYt1jzToaccfhDVKpmZg5Wu2Jqs2";
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/user").queryParam("firebaseUid", firebaseUid))
                .andExpect(status().isOk());
    }

    @Test
    public void getUserDuplicateCheck() throws Exception {
        String email = "xYt1jzToaccfhDVKpmZg5Wu2Jqs2";
        mockMvc.perform(
                        MockMvcRequestBuilders.get(String.format("/user/duplicate-check/%s", email)))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    public void getUserChartSetting() throws Exception {
        String uid = "1";
        mockMvc.perform(
                        MockMvcRequestBuilders.get(String.format("/user/%s/chart-setting", uid)))
                .andExpect(status().isOk());
    }

    @Test
    public void putUserChartSetting() throws Exception {
        String uid = "1";
        mockMvc.perform(
                        MockMvcRequestBuilders
                                .put(String.format("/user/%s/chart-setting", uid))
                                .queryParam("indicator", "test")
                )
                .andExpect(status().isOk());
    }
}
