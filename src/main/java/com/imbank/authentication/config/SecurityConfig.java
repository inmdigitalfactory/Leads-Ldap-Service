package com.imbank.authentication.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imbank.authentication.config.auth.AllowedAppsAuthenticationFilter;
import com.imbank.authentication.entities.AllowedApp;
import com.imbank.authentication.services.AllowedAppService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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
                .antMatchers("/configuration/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/swagger-ui.html",
                        "/webjars/**",
                        "/auth/login**",
                        "/api-docs/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/resources/**").permitAll()
                .anyRequest().authenticated()
        ;

    }

    public AllowedAppsAuthenticationFilter allowedAppsAuthenticationFilter() throws Exception {
        AllowedAppsAuthenticationFilter filter = new AllowedAppsAuthenticationFilter(allowedAppService);
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
        response.getWriter().println(e.getLocalizedMessage());
    }

    private void authenticationSuccessHandler(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        response.setStatus(HttpStatus.OK.value());

        log.info("Login Successful");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        AllowedApp allowedApp = (AllowedApp) authentication.getPrincipal();


        ObjectMapper objectMapper=new ObjectMapper();
        String r = objectMapper.writeValueAsString(allowedApp);

        response.getWriter().println(r);
    }

}
