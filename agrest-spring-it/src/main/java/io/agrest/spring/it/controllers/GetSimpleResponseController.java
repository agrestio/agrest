package io.agrest.spring.it.controllers;

import io.agrest.SimpleResponse;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    path = "simple",
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE)
public class GetSimpleResponseController {

  @GetMapping
  public SimpleResponse get() {
    return SimpleResponse.of(200, "Hi!");
  }

  @GetMapping(path = "2")
  public SimpleResponse get2() {
    return SimpleResponse.of(200, "Hi2!");
  }
}
