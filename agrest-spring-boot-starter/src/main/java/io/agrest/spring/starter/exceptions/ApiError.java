package io.agrest.spring.starter.exceptions;

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
