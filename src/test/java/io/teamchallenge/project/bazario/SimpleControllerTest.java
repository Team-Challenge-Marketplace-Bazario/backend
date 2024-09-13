package io.teamchallenge.project.bazario;

import io.teamchallenge.project.bazario.web.controller.SimpleController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(SimpleController.class)
class SimpleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void pingTest_get() throws Exception {
        mockMvc.perform(get("/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));


    }

    @Test
    void pingTest_post() throws Exception {
        mockMvc.perform(post("/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));


    }
}