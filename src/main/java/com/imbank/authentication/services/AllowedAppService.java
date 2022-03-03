package com.imbank.authentication.services;

import com.imbank.authentication.entities.AllowedApp;

public interface AllowedAppService {
    AllowedApp getApp(String accessToken);
}
