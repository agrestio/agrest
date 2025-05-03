package io.agrest.spring.it.controllers;

import io.agrest.DataResponse;
import io.agrest.runtime.AgRuntime;
import io.agrest.spring.it.pojo.model.P1;
import io.agrest.spring.it.pojo.model.P10;
import io.agrest.spring.it.pojo.model.P4;
import io.agrest.spring.it.pojo.model.P6;
import io.agrest.spring.it.pojo.model.P8;
import io.agrest.spring.it.pojo.model.P9;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static io.agrest.spring.it.controllers.ParamUtils.convertParams;

@RestController
@RequestMapping(
    path = "pojo",
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE)
public class GetPojoController {

  private final AgRuntime agRuntime;

  public GetPojoController(AgRuntime agRuntime) {this.agRuntime = agRuntime;}

  @GetMapping(path = "p1_empty")
  public DataResponse<P1> p1Empty() {
    return agRuntime.select(P1.class).getEmpty();
  }

  @GetMapping(path = "p1")
  public DataResponse<P1> p1All(@RequestParam Map<String,String> allParams) {
    return agRuntime.select(P1.class).clientParams(convertParams(allParams)).get();
  }

  @GetMapping(path = "p4")
  public DataResponse<P4> p4All(@RequestParam Map<String,String> allParams) {
    return agRuntime.select(P4.class).clientParams(convertParams(allParams)).get();
  }

  @GetMapping(path = "p6")
  public DataResponse<P6> p6All(@RequestParam Map<String,String> allParams) {
    return agRuntime.select(P6.class).clientParams(convertParams(allParams)).get();
  }

  @GetMapping(path = "p6/{id}")
  public DataResponse<P6> p6ById(@PathVariable("id") String id, @RequestParam Map<String,String> allParams) {
    return agRuntime.select(P6.class).clientParams(convertParams(allParams)).byId(id).get();
  }

  @GetMapping(path = "p8/{id}")
  public DataResponse<P8> p8ById(@PathVariable("id") int id, @RequestParam Map<String,String> allParams) {
    return agRuntime.select(P8.class).clientParams(convertParams(allParams)).byId(id).get();
  }

  @GetMapping(path = "p9")
  public DataResponse<P9> p9All(@RequestParam Map<String,String> allParams) {
    return agRuntime.select(P9.class).clientParams(convertParams(allParams)).get();
  }

  @GetMapping(path = "p10/{id1}/{id2}")
  public DataResponse<P10> p10ById(@PathVariable("id1") int id1, @PathVariable("id2") String id2, @RequestParam Map<String,String> allParams) {
    return agRuntime.select(P10.class).clientParams(convertParams(allParams)).byId(P10.id(id1, id2)).get();
  }
}

