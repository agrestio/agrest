package io.agrest.spring.it.controllers;

import io.agrest.AgException;
import io.agrest.DataResponse;
import io.agrest.SelectStage;
import io.agrest.runtime.AgRuntime;
import io.agrest.spring.it.pojo.model.P1;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(
    path = "nodata",
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE)
public class GetExceptionController {

  private final AgRuntime agRuntime;

  public GetExceptionController(AgRuntime agRuntime) {this.agRuntime = agRuntime;}


  @GetMapping
  public ResponseEntity<?> get() {
    throw AgException.notFound("Request failed");
  }

  @GetMapping(path = "th")
  public ResponseEntity<?> getTh() {
    try {
      throw new Throwable("Dummy");
    } catch (Throwable th) {
      throw AgException.internalServerError(th, "Request failed with th");
    }
  }

  @GetMapping(path = "rse")
  public ResponseEntity<?> getRse() {
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Was forbidden");
  }

  @GetMapping(path = "rse_inside_ag")
  public DataResponse<P1> getWaeInsideAg() {
    return agRuntime.select(P1.class)
        .stage(SelectStage.START, c -> {
          throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Was forbidden inside pipeline");
        }).get();
  }


}
