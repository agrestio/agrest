package io.agrest.spring.it.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GetPojoDataResponseControllerTest {

  @Autowired
  private MockMvc mvc;

  @Test
  public void test() throws Exception {
    mvc.perform(get("/data-response")
        .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json("{\"data\":[{\"a\":1,\"b\":\"two\",\"c\":{\"a\":5},\"d\":[{\"a\":6}]},{\"a\":100,\"b\":\"two hundred\",\"c\":{\"a\":50},\"d\":[{\"a\":60}]}],\"total\":2}"));
  }
}