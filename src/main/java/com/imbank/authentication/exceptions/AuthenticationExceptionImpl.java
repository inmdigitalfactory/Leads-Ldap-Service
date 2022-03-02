package com.imbank.authentication.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = true)
@Data
public class AuthenticationExceptionImpl extends RuntimeException {
    private HttpStatus httpStatus;
    private String message;

    public AuthenticationExceptionImpl(HttpStatus status, String message) {
        this.httpStatus = status;
        this.message = message;
    }

}
