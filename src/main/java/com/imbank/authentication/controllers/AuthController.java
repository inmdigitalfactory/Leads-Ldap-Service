package com.imbank.authentication.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imbank.authentication.dtos.LoginSuccessDTO;
import com.imbank.authentication.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("auth")
public class AuthController {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @RequestMapping("/token/refresh")
    private ResponseEntity<LoginSuccessDTO> refreshToken() {
        LoginSuccessDTO loginDto = authService.refreshToken();
        return ResponseEntity.ok().body(loginDto);
    }

    @RequestMapping("/token/validate")
    private ResponseEntity<Object> validateToken() {
        return ResponseEntity.ok().body(Map.of("valid", authService.isTokenValid()));
    }
}
