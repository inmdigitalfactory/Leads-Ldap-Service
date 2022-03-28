package com.imbank.authentication.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imbank.authentication.dtos.AuthDTO;
import com.imbank.authentication.dtos.LdapUserDTO;
import com.imbank.authentication.dtos.LoginSuccessDTO;
import com.imbank.authentication.entities.AllowedApp;
import com.imbank.authentication.exceptions.AuthenticationExceptionImpl;
import com.imbank.authentication.repositories.AllowedAppRepository;
import com.imbank.authentication.services.AuthService;
import com.imbank.authentication.utils.AuthUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static com.imbank.authentication.utils.Constants.*;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final RestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AllowedAppRepository allowedAppRepository;

    @Value("${server.port}")
    private int port;

    public AuthServiceImpl() {
        this.restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {

            }
        });
    }

    @Override
    public LoginSuccessDTO refreshToken() {
        Claims claims = (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (ObjectUtils.isEmpty(claims) || !claims.get(CLAIM_FUNCTION).equals(FUNCTION_REFRESH_TOKEN)) {
            throw new IllegalArgumentException("You can use only the refresh token");
        }
        LdapUserDTO user = objectMapper.convertValue(claims.get(CLAIM_USER), LdapUserDTO.class);
        AllowedApp app = objectMapper.convertValue(claims.get(CLAIM_APP), AllowedApp.class);
        return AuthUtils.getLoginSuccessDTO(user, app);
    }

    @Override
    public boolean isTokenValid() {
        Claims claims = (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return !ObjectUtils.isEmpty(claims);
    }

    @Override
    public ResponseEntity<LoginSuccessDTO> appLogin(AuthDTO authDto) {
        AllowedApp thisApp = allowedAppRepository.findFirstByName(APP_NAME)
                .orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.INTERNAL_SERVER_ERROR, "Credentials for this service not found and therefore login is no longer possible. Ensure the application details have not been edited"));
        String url = String.format("http://localhost:%d/auth/login", port);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + thisApp.getAccessToken());
        HttpEntity<AuthDTO> httpEntity = new HttpEntity<>(authDto, headers);
        ResponseEntity<LoginSuccessDTO> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, LoginSuccessDTO.class);
        return response.getStatusCode().is2xxSuccessful()
                ? ResponseEntity.ok().body(response.getBody())
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginSuccessDTO());
    }
}
