package com.imbank.authentication.services;

import com.imbank.authentication.dtos.AuthDTO;
import com.imbank.authentication.dtos.LoginSuccessDTO;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    LoginSuccessDTO refreshToken();

    boolean isTokenValid();

    ResponseEntity<LoginSuccessDTO> appLogin(AuthDTO authDto);
}
