package io.agrest.spring.it.controllers;

import io.agrest.spring.it.pojo.model.P1;
import io.agrest.spring.it.pojo.model.P10;
import io.agrest.spring.it.pojo.model.P3;
import io.agrest.spring.it.pojo.model.P4;
import io.agrest.spring.it.pojo.model.P6;
import io.agrest.spring.it.pojo.model.P8;
import io.agrest.spring.it.pojo.model.P9;
import io.agrest.spring.it.pojo.runtime.PojoStore;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
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
class GetPojoControllerTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private PojoStore pojoStore;

  @BeforeEach
  void setUp() {
    pojoStore.clear();
  }

  @Test
  public void testById() throws Exception {

    P6 o1 = new P6();
    o1.setIntProp(15);
    o1.setStringId("o1id");
    P6 o2 = new P6();
    o2.setIntProp(16);
    o2.setStringId("o2id");

    pojoStore.bucket(P6.class).put("o1id", o1);
    pojoStore.bucket(P6.class).put("o2id", o2);

    mvc.perform(get("/pojo/p6/o2id")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json("{\"data\":[{\"id\":\"o2id\",\"intProp\":16}],\"total\":1}"));
  }

  @Test
  public void testById_MultiKey() throws Exception {

    P10 o1 = new P10();
    o1.setId1(5);
    o1.setId2("six");
    o1.setA1("seven");

    P10 o2 = new P10();
    o2.setId1(8);
    o2.setId2("nine");
    o2.setA1("ten");

    pojoStore.bucket(P10.class).put(o1.id(), o1);
    pojoStore.bucket(P10.class).put(o2.id(), o2);

    mvc.perform(get("/pojo/p10/5/six")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json("{\"data\":[{\"id\":{\"id1\":5,\"id2\":\"six\"},\"a1\":\"seven\"}],\"total\":1}"));
  }


  @Test
  public void testByQueryParam() throws Exception {

    P6 o1 = new P6();
    o1.setIntProp(15);
    o1.setStringId("o1id");
    P6 o2 = new P6();
    o2.setIntProp(16);
    o2.setStringId("o2id");

    pojoStore.bucket(P6.class).put("o1id", o1);
    pojoStore.bucket(P6.class).put("o2id", o2);

    mvc.perform(get("/pojo/p6")
            .contentType(MediaType.APPLICATION_JSON)
            .param("sort", "id"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json("{\"data\":[{\"id\":\"o1id\",\"intProp\":15},{\"id\":\"o2id\",\"intProp\":16}],\"total\":2}"));
  }

  @Test
  public void testIncludeToOne() throws Exception {

    P3 o0 = new P3();
    o0.setName("xx3");

    P4 o1 = new P4();
    o1.setP3(o0);

    pojoStore.bucket(P4.class).put("o1id", o1);

    mvc.perform(get("/pojo/p4")
            .contentType(MediaType.APPLICATION_JSON)
            .param("include", "p3"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json("{\"data\":[{\"p3\":{\"name\":\"xx3\"}}],\"total\":1}"));
  }

  @Test
  public void testNoId() throws Exception {

    P1 o1 = new P1();
    o1.setName("n2");
    P1 o2 = new P1();
    o2.setName("n1");

    pojoStore.bucket(P1.class).put("o1id", o1);
    pojoStore.bucket(P1.class).put("o2id", o2);

    mvc.perform(get("/pojo/p1")
            .contentType(MediaType.APPLICATION_JSON)
            .param("sort", "name"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json("{\"data\":[{\"name\":\"n1\"},{\"name\":\"n2\"}],\"total\":2}"));

  }

  @Test
  public void testWithTime() throws Exception {

    P9 o9 = new P9();
    o9.setName("p9name1");
    LocalDateTime ldt = LocalDateTime.of(1999, 10, 2, 12, 54, 31);
    o9.setCreated(OffsetDateTime.of(ldt, ZoneOffset.ofHours(3)));
    o9.setCreatedLocal(ldt);

    pojoStore.bucket(P9.class).put("o9id", o9);

    mvc.perform(get("/pojo/p9")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json("{\"data\":[{\"created\":\"1999-10-02T12:54:31+03:00\",\"createdLocal\":\"1999-10-02T12:54:31\",\"name\":\"p9name1\"}],\"total\":1}"));
  }

  @Test
  public void testMapBy() throws Exception {

    P1 o1 = new P1();
    o1.setName("n2");
    P1 o2 = new P1();
    o2.setName("n1");

    pojoStore.bucket(P1.class).put("o1id", o1);
    pojoStore.bucket(P1.class).put("o2id", o2);

    mvc.perform(get("/pojo/p1")
            .contentType(MediaType.APPLICATION_JSON)
            .param("mapBy", "name"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json("{\"data\":{\"n1\":[{\"name\":\"n1\"}],\"n2\":[{\"name\":\"n2\"}]},\"total\":2}"));
  }

  @Test
  public void testCollectionAttributes() throws Exception {

    P8 o1 = new P8();
    o1.setId(1);
    o1.setBooleans(List.of(true, false));
    o1.setCharacters(List.of('a', 'b', 'c'));
    o1.setDoubles(List.of(1., 2.5, 3.5));
    o1.setStringSet(Set.of("abc"));

    List<Number> numbers = Arrays.asList((byte) 0, (short) 1, 2, 3L, 4.f, 5.);
    o1.setNumberList(numbers);
    o1.setWildcardCollection(numbers);

    pojoStore.bucket(P8.class).put(1, o1);

    mvc.perform(get("/pojo/p8/1")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json("{\n" +
            "  \"data\": [\n" +
            "    {\n" +
            "      \"id\": 1,\n" +
            "      \"booleans\": [\n" +
            "        true,\n" +
            "        false\n" +
            "      ],\n" +
            "      \"characters\": [\n" +
            "        \"a\",\n" +
            "        \"b\",\n" +
            "        \"c\"\n" +
            "      ],\n" +
            "      \"doubles\": [\n" +
            "        1.0,\n" +
            "        2.5,\n" +
            "        3.5\n" +
            "      ],\n" +
            "      \"genericCollection\": [],\n" +
            "      \"numberList\": [\n" +
            "        0,\n" +
            "        1,\n" +
            "        2,\n" +
            "        3,\n" +
            "        4.0,\n" +
            "        5.0\n" +
            "      ],\n" +
            "      \"stringSet\": [\n" +
            "        \"abc\"\n" +
            "      ],\n" +
            "      \"wildcardCollection\": [\n" +
            "        0,\n" +
            "        1,\n" +
            "        2,\n" +
            "        3,\n" +
            "        4.0,\n" +
            "        5.0\n" +
            "      ]\n" +
            "    }\n" +
            "  ],\n" +
            "  \"total\": 1\n" +
            "}"));
  }

  @Test
  public void testGetEmpty() throws Exception {

    P1 o1 = new P1();
    o1.setName("n2");


    pojoStore.bucket(P1.class).put("o1id", o1);

    mvc.perform(get("/pojo/p1_empty")
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().json("{\"data\":[],\"total\":0}"));
  }
}