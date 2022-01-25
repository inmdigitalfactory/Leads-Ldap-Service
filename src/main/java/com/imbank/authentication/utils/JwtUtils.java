package com.imbank.authentication.utils;

import com.imbank.authentication.dtos.LdapUserDTO;
import com.imbank.authentication.entities.AllowedApp;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static com.imbank.authentication.utils.Constants.*;

@Slf4j
public class JwtUtils {

    private static String privateKey;

    static {
        try {
            privateKey = FileUtils.readFile("private.key");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String createToken(LdapUserDTO user, AllowedApp app, String ipAddress, boolean isRefreshToken) {
        long now = (new Date()).getTime();
        Date validity = new Date(now + (isRefreshToken ? app.getRefreshTokenValiditySeconds() : app.getTokenValiditySeconds()) * 1000);

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuer(Constants.ISSUER)
                .setAudience(app.getName())
                .setIssuedAt(new Date())
                .claim( CLAIM_USER, user)
                .claim(CLAIM_APP, app)
                .claim(CLAIM_FUNCTION, isRefreshToken ? FUNCTION_REFRESH_TOKEN : FUNCTION_TOKEN)
                .claim(CLAIM_IP, ipAddress)
                .signWith(SignatureAlgorithm.HS256, privateKey.getBytes(StandardCharsets.UTF_8))
                .setExpiration(validity)
                .compact();
    }

    public static Claims validateToken(String authToken) {
        try {
            return Jwts.parser()
                    .setSigningKey(privateKey.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(authToken)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token.", e);
        }
        return null;
    }

}
