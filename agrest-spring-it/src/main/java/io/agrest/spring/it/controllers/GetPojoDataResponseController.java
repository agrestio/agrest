package io.agrest.spring.it.controllers;

import io.agrest.DataResponse;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    path = "data-response",
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE)
public class GetPojoDataResponseController {

  @GetMapping
  public DataResponse<X> xs() {
    // generate response bypassing Agrest stack
    return DataResponse.of(List.of(
        new X(1, "two", new Y(5), List.of(new Y(6))),
        new X(100, "two hundred", new Y(50), List.of(new Y(60))))).build();
  }


  public static class X {
    private final int a;
    private final String b;
    private final Y c;
    private final List<Y> d;

    public X(int a, String b, Y c, List<Y> d) {
      this.a = a;
      this.b = b;
      this.c = c;
      this.d = d;
    }

    public int getA() {
      return a;
    }

    public String getB() {
      return b;
    }

    public Y getC() {
      return c;
    }

    public List<Y> getD() {
      return d;
    }
  }

  public static class Y {
    private final int a;

    public Y(int a) {
      this.a = a;
    }

    public int getA() {
      return a;
    }
  }

}
