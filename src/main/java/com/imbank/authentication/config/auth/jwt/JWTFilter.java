package com.imbank.authentication.config.auth.jwt;

import com.imbank.authentication.utils.Constants;
import com.imbank.authentication.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Locale;

/**
 * Filters incoming requests and installs a Spring Security principal if a header corresponding to a valid user is
 * found.
 */
@Slf4j
//@Component
public class JWTFilter extends GenericFilterBean {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
//        log.info("URI is Path->{} : uri->{} : url->{}", httpServletRequest.getPathInfo(), httpServletRequest.getRequestURI(), httpServletRequest.getRequestURL());
        if(!"/saml/logout".endsWith(httpServletRequest.getRequestURI())) {
            String jwt = resolveToken(httpServletRequest);

            Authentication authentication = getAuthentication(jwt);
            if (!ObjectUtils.isEmpty(authentication)) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("User authentication successful: {}", authentication);
            }
            else {
                log.info("Authentication failed");
//                throw new AuthenticationCredentialsNotFoundException("No authentication provided");
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private String resolveToken(HttpServletRequest request){
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.toLowerCase(Locale.ROOT).startsWith("bearer ")) {
            return bearerToken.substring(7);
        }
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            for(Cookie cookie:cookies) {
                log.info("Processing cookie {}", cookie.getName());
                if(Constants.TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public Authentication getAuthentication(String token) {
        Claims claims = JwtUtils.validateToken(token);
        return claims == null ? null : new UsernamePasswordAuthenticationToken(claims, token, null);
    }
}
