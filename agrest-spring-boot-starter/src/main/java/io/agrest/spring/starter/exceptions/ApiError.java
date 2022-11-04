package io.agrest.spring.starter.exceptions;

import org.springframework.http.HttpStatus;

public class ApiError {

  private final String message;

  public ApiError(String message) {
    super();
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
