package io.agrest.spring.it.controllers;

import io.agrest.AgException;
import io.agrest.DataResponse;
import io.agrest.SelectStage;
import io.agrest.runtime.AgRuntime;
import io.agrest.spi.AgExceptionMapper;
import io.agrest.spring.it.pojo.model.P1;
import io.agrest.spring.it.pojo.model.P2;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE)
public class ExceptionMappersController {

  private final AgRuntime agRuntime;

  public ExceptionMappersController(AgRuntime agRuntime) {
    this.agRuntime = agRuntime;
  }

  @GetMapping(path = "agexception")
  public DataResponse<P1> agException() {
    return agRuntime.select(P1.class)
        .stage(SelectStage.APPLY_SERVER_PARAMS, c -> {
          throw AgException.forbidden("_ag_exception_");
        })
        .get();
  }

  @GetMapping(path = "testexception")
  public DataResponse<P2> testException() {
    return agRuntime.select(P2.class)
        .stage(SelectStage.APPLY_SERVER_PARAMS, c -> {
          throw new TestException("_test_exception_");
        })
        .get();
  }

  public static class TestAgExceptionMapper implements AgExceptionMapper<AgException> {

    @Override
    public AgException toAgException(AgException e) {
      return AgException.internalServerError(e, "_ag_%s", e.getMessage());
    }
  }

  public static class TestExceptionMapper implements AgExceptionMapper<TestException> {

    @Override
    public AgException toAgException(TestException e) {
      return AgException.internalServerError(e, "_test_%s", e.getMessage());
    }
  }

  public static class TestException extends RuntimeException {
    public TestException(String message) {
      super(message);
    }
  }
}
