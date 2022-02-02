package com.imbank.authentication.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imbank.authentication.dtos.LdapUserDTO;
import com.imbank.authentication.dtos.LoginSuccessDTO;
import com.imbank.authentication.entities.AllowedApp;
import com.imbank.authentication.utils.AuthUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import static com.imbank.authentication.utils.Constants.*;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private ObjectMapper objectMapper;

    public LoginSuccessDTO refreshToken() {
        Claims claims = (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(ObjectUtils.isEmpty(claims) || !claims.get(CLAIM_FUNCTION).equals(FUNCTION_REFRESH_TOKEN)) {
            throw new IllegalArgumentException("You can use only the refresh token");
        }
        LdapUserDTO user = objectMapper.convertValue(claims.get(CLAIM_USER), LdapUserDTO.class);
        AllowedApp app = objectMapper.convertValue(claims.get(CLAIM_APP), AllowedApp.class);
        return AuthUtils.getLoginSuccessDTO(user, app);
    }

    public boolean isTokenValid() {
        Claims claims = (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return !ObjectUtils.isEmpty(claims);
    }

}
