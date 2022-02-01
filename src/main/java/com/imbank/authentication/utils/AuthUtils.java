package com.imbank.authentication.utils;

import com.imbank.authentication.dtos.LdapUserDTO;
import com.imbank.authentication.dtos.LoginSuccessDTO;
import com.imbank.authentication.entities.AllowedApp;

public class AuthUtils {


    public static LoginSuccessDTO getLoginSuccessDTO(LdapUserDTO ldapUserDTO, AllowedApp allowedApp) {
        String ipAddress = RequestUtils.getClientIpAddress();

        LoginSuccessDTO loginSuccessDTO = new LoginSuccessDTO();
        loginSuccessDTO.setToken(JwtUtils.createToken(ldapUserDTO, allowedApp, ipAddress, false));
        loginSuccessDTO.setRefreshToken(JwtUtils.createToken(ldapUserDTO, allowedApp, ipAddress, true));
        loginSuccessDTO.setTokenValidity(allowedApp.getTokenValiditySeconds());
        loginSuccessDTO.setRefreshTokenValidity(allowedApp.getRefreshTokenValiditySeconds());
        return loginSuccessDTO;
    }

}
