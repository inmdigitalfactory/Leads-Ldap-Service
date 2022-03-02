package com.imbank.authentication.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
@Slf4j
public class DefaultExceptionHandler {


    @ExceptionHandler(AuthenticationExceptionImpl.class)
    public ResponseEntity<String> handleAuthenticationException(WebRequest request, AuthenticationExceptionImpl authenticationException) {
        return ResponseEntity.status(authenticationException.getHttpStatus()).body(authenticationException.getMessage());
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<String> handleUncaughtExceptions(WebRequest request, Throwable exception) {
        log.error("Uncaught error occurred: ", exception);
        return ResponseEntity.internalServerError().body("There was an error processing your request");
    }
}
