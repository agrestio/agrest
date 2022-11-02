package io.agrest.spring.it;

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
