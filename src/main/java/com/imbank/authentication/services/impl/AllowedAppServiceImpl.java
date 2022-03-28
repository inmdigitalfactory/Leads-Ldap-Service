package com.imbank.authentication.services.impl;

import com.imbank.authentication.entities.AllowedApp;
import com.imbank.authentication.repositories.AllowedAppRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AllowedAppServiceImpl implements com.imbank.authentication.services.AllowedAppService {

    @Autowired
    private AllowedAppRepository allowedAppRepository;

    @Override
    public AllowedApp getApp(String accessToken) {
        return allowedAppRepository.findFirstByAccessTokenAndEnabled(accessToken, true);
    }
}
