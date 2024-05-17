package com.mztrade.hki;

import com.mztrade.hki.controller.UserController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserTests {
    @Autowired
    UserController userController;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getPostsList() throws Exception {
        String firebaseUid = "I_AM_FIREBASE";
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/user").queryParam("firebaseUid", firebaseUid))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }
}
