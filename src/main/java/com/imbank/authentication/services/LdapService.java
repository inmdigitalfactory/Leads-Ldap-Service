package com.imbank.authentication.services;

import com.imbank.authentication.dtos.AuthDTO;
import com.imbank.authentication.dtos.LdapUserDTO;

public interface LdapService {
    LdapUserDTO getADDetails(AuthDTO credentials);
    LdapUserDTO getADDetails(String adUsername);
}
