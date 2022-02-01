package com.imbank.authentication.config.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imbank.authentication.dtos.AuthDTO;
import com.imbank.authentication.dtos.LdapUserDTO;
import com.imbank.authentication.entities.AllowedApp;
import com.imbank.authentication.services.AllowedAppService;
import com.imbank.authentication.services.LdapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class AllowedAppsAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    private final AllowedAppService allowedAppService;
    private final ObjectMapper objectMapper;
    private final LdapService ldapService;

    public AllowedAppsAuthenticationFilter(AllowedAppService allowedAppService, LdapService ldapService) {
        this.allowedAppService = allowedAppService;
        this.ldapService = ldapService;

        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        String authHeader = getAuthHeader(request);
        if (authHeader == null) {
            throw new AuthenticationCredentialsNotFoundException("Unknown Application. Please authenticate");
        }

        AllowedApp allowedApp = allowedAppService.getApp(authHeader);
        if (allowedApp == null) {
            throw new AuthenticationCredentialsNotFoundException("Unknown Application. Invalid Credentials or App Inactive");
        }

        UsernamePasswordAuthenticationToken authRequest = getAuthRequest(allowedApp, request);
        setDetails(request, authRequest);
        log.info("Valid app: {}", allowedApp);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    protected AuthenticationManager getAuthenticationManager() {
        return authentication -> {
            //perform ldap authentication
            log.info("Performing ldap authentication");
            AuthDTO authDTO = (AuthDTO) authentication.getCredentials();
            AllowedApp allowedApp = (AllowedApp) authentication.getPrincipal();
            LdapUserDTO details = ldapService.getADDetails(authDTO);
            if(ObjectUtils.isEmpty(details)) {
                throw new AuthenticationCredentialsNotFoundException("Unknown LDAP User. Invalid Credentials");
            }
            return new UsernamePasswordAuthenticationToken(allowedApp, details);
        };
    }


    private UsernamePasswordAuthenticationToken getAuthRequest(AllowedApp allowedApp, HttpServletRequest request) {
        AuthDTO authDto = null;
        try {
            authDto = objectMapper.readValue(request.getReader(), AuthDTO.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new UsernamePasswordAuthenticationToken(allowedApp, authDto);
    }

    private String getAuthHeader(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if(!ObjectUtils.isEmpty(authHeader) && authHeader.length() > 7){
            return authHeader.substring(7);
        }
        return authHeader;
    }
}
