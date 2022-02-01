package com.imbank.authentication.controllers;

import com.imbank.authentication.dtos.AuthDTO;
import com.imbank.authentication.dtos.LoginSuccessDTO;
import com.imbank.authentication.dtos.TokenValidDto;
import com.imbank.authentication.services.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
public class AuthController {


    @Autowired
    private AuthService authService;

    /**
     * This is a fake login api. The actual login logic is handled by spring security. This fake endpoint just documents the login
     * process on swagger ui
     * @return the login payload
     */
    @SecurityRequirement(name = "Application Access Token")
    @PostMapping("/login")
    public String login(@RequestBody AuthDTO authDto) {
        return "";
    }

    @RequestMapping("/token/refresh")
    private ResponseEntity<LoginSuccessDTO> refreshToken() {
        LoginSuccessDTO loginDto = authService.refreshToken();
        return ResponseEntity.ok().body(loginDto);
    }

    @RequestMapping("/token/validate")
    private ResponseEntity<TokenValidDto> validateToken() {
        return ResponseEntity.ok().body(new TokenValidDto(authService.isTokenValid()));
    }
}
