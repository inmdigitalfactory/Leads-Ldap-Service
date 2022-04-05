package com.imbank.authentication.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imbank.authentication.dtos.LdapUserDTO;
import com.imbank.authentication.dtos.LoginSuccessDTO;
import com.imbank.authentication.entities.AllowedApp;
import com.imbank.authentication.entities.SystemAccess;
import com.imbank.authentication.enums.AppPermission;
import com.imbank.authentication.exceptions.AuthenticationExceptionImpl;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;

import java.util.*;

import static com.imbank.authentication.utils.Constants.CLAIM_PERMISSIONS;

@Slf4j
public class AuthUtils {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
    }

    public static LoginSuccessDTO getLoginSuccessDTO(LdapUserDTO ldapUserDTO, AllowedApp allowedApp) {
        String ipAddress = RequestUtils.getClientIpAddress();

        LoginSuccessDTO loginSuccessDTO = new LoginSuccessDTO();
        loginSuccessDTO.setToken(JwtUtils.createToken(ldapUserDTO, allowedApp, ipAddress, false));
        loginSuccessDTO.setRefreshToken(JwtUtils.createToken(ldapUserDTO, allowedApp, ipAddress, true));
        loginSuccessDTO.setTokenValidity(allowedApp.getTokenValiditySeconds());
        loginSuccessDTO.setRefreshTokenValidity(allowedApp.getRefreshTokenValiditySeconds());
        return loginSuccessDTO;
    }

    public static String generateAccessToken() {
        return UUID.randomUUID().toString();
    }

    private static boolean hasAnyPermission(List<String> permissions) {
        Claims claims = (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(ObjectUtils.isEmpty(claims)) {
            return false;
        }
        Map<String, String> permMap = new HashMap<>();
        for(String perm: permissions) {
            permMap.put(perm, perm);
        }
        List<String> userPermissions = objectMapper.convertValue(claims.get(CLAIM_PERMISSIONS), new TypeReference<>() {});
        return userPermissions.stream().anyMatch(permMap::containsKey);
    }

    public static void ensurePermitted(AllowedApp allowedApp, List<AppPermission> permissions ) {
        List<String> allPermissions = new ArrayList<>();
        permissions.forEach(perm -> {
            if(allowedApp != null) {
                allPermissions.add(String.format("%s_%d", perm.name(), allowedApp.getId()));
            }
            allPermissions.add(perm.name());
        });
        if(!hasAnyPermission(allPermissions)) {
            throw new AuthenticationExceptionImpl(HttpStatus.FORBIDDEN, "You do not have the necessary permissions");
        }
    }

    public static void ensurePermitted(long id, List<AppPermission> permissions ) {
        AllowedApp app = new AllowedApp();
        app.setId(id);
        ensurePermitted(app, permissions);
    }

    public static void ensurePermitted(Set<SystemAccess> systemAccesses, List<AppPermission> permissions) {
        boolean permitted = false;
        for(SystemAccess systemAccess: systemAccesses){
            try {
                ensurePermitted(systemAccess.getApp(), permissions);
                permitted = true;
                break;
            }
            catch (Exception ignored) {}
        }
        if(!permitted) {
            throw new AuthenticationExceptionImpl(HttpStatus.FORBIDDEN, "You do not have the necessary permissions");
        }
    }

    public static Optional<String> getLoggedInUser() {
        try {
            Claims claims = (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            log.info("Currently logged in user is {} - {}", claims.getSubject(), claims);
            return Optional.ofNullable(claims.getSubject());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
