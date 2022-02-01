package com.imbank.authentication.dtos;

import lombok.Data;

@Data
public class LoginSuccessDTO {
    private String token;
    private String refreshToken;
    private long tokenValidity;
    private long refreshTokenValidity;
}
