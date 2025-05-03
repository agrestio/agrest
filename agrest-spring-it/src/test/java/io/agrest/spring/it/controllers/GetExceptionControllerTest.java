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
class GetExceptionControllerTest {

  @Autowired
  private MockMvc mvc;

  @Test
  public void testNoData() throws Exception {
    mvc.perform(get("/nodata")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isNotFound())
        .andExpect(content().json("{\"message\":\"Request failed\"}"));
  }

  @Test
  public void testNoData_WithThrowable() throws Exception {
    mvc.perform(get("/nodata/th")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(content().json("{\"message\":\"Request failed with th\"}"));
  }

  @Test
  public void testNoData_WithResponseStatusException() throws Exception {
    mvc.perform(get("/nodata/rse")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isForbidden());
  }

  @Test
  public void testNoData_WithResponseStatusException_InsidePipeline() throws Exception {
    mvc.perform(get("/nodata/rse_inside_ag")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isForbidden());
  }
}