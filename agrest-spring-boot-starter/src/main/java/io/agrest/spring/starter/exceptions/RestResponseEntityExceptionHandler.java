package io.agrest.spring.starter.exceptions;

import io.agrest.AgException;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(value = {AgException.class})
  protected ResponseEntity<Object> handleAgException(AgException ex, WebRequest request) {
    Throwable cause = ex.getCause() != null && ex.getCause() != ex ? ex.getCause() : null;
    if (cause instanceof ResponseStatusException) {
      var rse = (ResponseStatusException) cause;
      return new ResponseEntity<>(rse.getLocalizedMessage(), rse.getStatus());
    }

    var apiError = new ApiError(ex.getMessage());
    return new ResponseEntity<>(apiError, HttpStatus.resolve(ex.getStatus()));
  }
}
