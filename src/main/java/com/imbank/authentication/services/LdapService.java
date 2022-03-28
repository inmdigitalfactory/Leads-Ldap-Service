package com.imbank.authentication.services;

import com.imbank.authentication.dtos.AuthDTO;
import com.imbank.authentication.dtos.LdapUserDTO;
import com.imbank.authentication.entities.AllowedApp;

public interface LdapService {
    LdapUserDTO getADDetails(AllowedApp app, AuthDTO credentials);
    LdapUserDTO getADDetails(String adUsername, String ou);
}
