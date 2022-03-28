package com.imbank.authentication.services;

import com.imbank.authentication.dtos.AllowedAppDto;
import com.imbank.authentication.dtos.LdapUserDTO;
import com.imbank.authentication.entities.AllowedApp;

import java.util.List;

public interface AppService {
    AllowedApp createApp(AllowedAppDto allowedAppDto);
    String resetToken(long id);
    AllowedApp updateApp(long id, AllowedAppDto allowedAppDto);
    List<AllowedApp> getApps();
    AllowedApp getApp(long id);
    void deleteApp(long id);
}
