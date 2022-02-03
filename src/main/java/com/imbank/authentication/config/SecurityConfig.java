package com.imbank.authentication.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imbank.authentication.config.auth.AllowedAppsAuthenticationFilter;
import com.imbank.authentication.config.auth.jwt.JWTConfigurer;
import com.imbank.authentication.dtos.ApiResponse;
import com.imbank.authentication.dtos.LdapUserDTO;
import com.imbank.authentication.dtos.LoginSuccessDTO;
import com.imbank.authentication.entities.AllowedApp;
import com.imbank.authentication.services.AllowedAppService;
import com.imbank.authentication.services.LdapService;
import com.imbank.authentication.utils.AuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Slf4j
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AllowedAppService allowedAppService;

    @Autowired
    private LdapService ldapService;

    @Value("${security.endpoints.allow}")
    private String[] safeEndpoints;

    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
                .antMatchers(safeEndpoints);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf()
                .disable()
                .cors()
                .and()
                .addFilterBefore(allowedAppsAuthenticationFilter(), AllowedAppsAuthenticationFilter.class)
                .exceptionHandling()
                .and()
                .authorizeRequests()
                .antMatchers(safeEndpoints).permitAll()
                .anyRequest().authenticated()
                .and()
                .apply(securityConfigurerAdapter())
        ;

    }

    public AllowedAppsAuthenticationFilter allowedAppsAuthenticationFilter() throws Exception {
        AllowedAppsAuthenticationFilter filter = new AllowedAppsAuthenticationFilter(allowedAppService, ldapService);
        filter.setFilterProcessesUrl("/auth/login");
        filter.setAuthenticationManager(authenticationManagerBean());
        filter.setAuthenticationFailureHandler(this::authenticationFailureHandler);
        filter.setAuthenticationSuccessHandler(this::authenticationSuccessHandler);
        log.info("Default auth filter initialized");
        return filter;
    }


    private void authenticationFailureHandler(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException e) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        log.info("Error authenticating: {}", e.getLocalizedMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> apiResponse = new ApiResponse<>("-1", e.getLocalizedMessage(), null);
        ObjectMapper objectMapper=new ObjectMapper();
        String r = objectMapper.writeValueAsString(apiResponse);

        response.getWriter().println(r);
    }

    private void authenticationSuccessHandler(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        response.setStatus(HttpStatus.OK.value());

        log.info("Login Successful");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        LdapUserDTO ldapUserDTO = (LdapUserDTO) authentication.getCredentials();
        AllowedApp allowedApp = (AllowedApp) authentication.getPrincipal();
        LoginSuccessDTO loginSuccessDTO = AuthUtils.getLoginSuccessDTO(ldapUserDTO, allowedApp);

        ObjectMapper objectMapper=new ObjectMapper();
        String r = objectMapper.writeValueAsString(loginSuccessDTO);

        response.getWriter().println(r);
    }


    private JWTConfigurer securityConfigurerAdapter() {
        return new JWTConfigurer();
    }

}
