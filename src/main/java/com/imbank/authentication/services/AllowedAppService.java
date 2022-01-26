package com.imbank.authentication.services;

import com.imbank.authentication.entities.AllowedApp;
import com.imbank.authentication.repositories.AllowedAppRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AllowedAppService {

    @Autowired
    private AllowedAppRepository allowedAppRepository;

    public AllowedApp getApp(String accessToken) {
        return allowedAppRepository.findFirstByAccessTokenAndEnabled(accessToken, true);
    }
}
