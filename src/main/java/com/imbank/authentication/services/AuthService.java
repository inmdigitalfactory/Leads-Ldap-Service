package com.imbank.authentication.services;

import com.imbank.authentication.dtos.LoginSuccessDTO;

public interface AuthService {
    LoginSuccessDTO refreshToken();

    boolean isTokenValid();
}
