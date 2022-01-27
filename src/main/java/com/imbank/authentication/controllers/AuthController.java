package com.imbank.authentication.controllers;

import com.imbank.authentication.dtos.AuthDTO;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
public class AuthController {

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
}
