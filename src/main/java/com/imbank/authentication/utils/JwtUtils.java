package com.imbank.authentication.utils;

import com.imbank.authentication.dtos.LdapUserDTO;
import com.imbank.authentication.entities.AllowedApp;
import com.imbank.authentication.entities.Role;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
        boolean isLdapService = APP_NAME.equals(app.getName());
        JwtBuilder jwt = Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuer(ISSUER)
                .setAudience(app.getName())
                .setIssuedAt(new Date())
                .claim( CLAIM_USER, user)
                .claim(CLAIM_APP, app)
                .claim(CLAIM_FUNCTION, isRefreshToken ? FUNCTION_REFRESH_TOKEN : FUNCTION_TOKEN)
                .claim(CLAIM_IP, ipAddress);

        if(!ObjectUtils.isEmpty(user.getUser())) {
                jwt.claim(CLAIM_ROLES, user.getUser().getSystemAccesses().stream()
                        .filter(systemAccess -> systemAccess.getApp().getId().equals(app.getId()))
                        .map(systemAccess -> systemAccess.getRoles().stream().map(Role::getName).collect(Collectors.joining()))
                        .collect(Collectors.toSet()));
                List<String> permissions = new ArrayList<>();
                if(isLdapService) {
                    //permissions are stored as <permission_code><target_app_id> for
                    user.getUser()
                            .getSystemAccesses()
                            .stream().filter(systemAccess -> systemAccess.getApp().getId().equals(app.getId()))
                            .forEach(userRole ->
                                    userRole.getRoles()
                                            .forEach(role -> role.getPermissions().forEach(permission -> {
                                                if(!ObjectUtils.isEmpty(userRole.getApps())) {
                                                    userRole.getApps()
                                                            .forEach(a ->
                                                                    permissions
                                                                            .add(String.format("%s_%d", permission.getCode(), a.getId())));
                                                }
                                                else {
                                                    permissions.add(permission.getCode());
                                                }
                                            })));
                }
                else {
                    user.getUser()
                            .getSystemAccesses()
                            .stream().filter(systemAccess -> systemAccess.getApp().getId().equals(app.getId()))
                            .forEach(userRole ->
                                    userRole.getRoles()
                                            .forEach(role -> role.getPermissions().forEach(permission ->
                                                    permissions.add(permission.getCode())))
                                           );
                }
                jwt.claim(CLAIM_PERMISSIONS, permissions);
        }

        return jwt.signWith(SignatureAlgorithm.HS256, privateKey.getBytes(StandardCharsets.UTF_8))
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
            log.error("Invalid JWT token. {}", e.getLocalizedMessage());
        }
        return null;
    }

}
