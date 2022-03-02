package com.imbank.authentication.services.impl;

import com.imbank.authentication.dtos.AllowedAppDto;
import com.imbank.authentication.entities.AllowedApp;
import com.imbank.authentication.exceptions.AuthenticationExceptionImpl;
import com.imbank.authentication.repositories.AllowedAppRepository;
import com.imbank.authentication.services.AppService;
import com.imbank.authentication.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppServiceImpl implements AppService {

    @Autowired
    private AllowedAppRepository allowedAppRepository;

    @Override
    public AllowedApp createApp(AllowedAppDto allowedAppDto) {
        if(allowedAppRepository.findFirstByName(allowedAppDto.getName()).isPresent()) {
            throw new AuthenticationExceptionImpl(HttpStatus.BAD_REQUEST,"App already exists");
        }
        AllowedApp allowedApp = new AllowedApp();
        allowedApp.setName(allowedAppDto.getName());
        allowedApp.setRefreshTokenValiditySeconds(allowedAppDto.getRefreshTokenValiditySeconds());
        String accessToken = AuthUtils.generateAccessToken();
        allowedApp.setAccessToken(accessToken);
        allowedApp.setTokenValiditySeconds(allowedAppDto.getTokenValiditySeconds());
        allowedApp.setEnabled(allowedAppDto.isEnabled());

        return allowedAppRepository.save(allowedApp);
    }

    @Override
    public String resetToken(long id) {
        AllowedApp allowedApp = allowedAppRepository.findById(id).orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "No such application"));
        String accessToken = AuthUtils.generateAccessToken();
        allowedApp.setAccessToken(accessToken);
        allowedAppRepository.save(allowedApp);
        return accessToken;
    }

    @Override
    public AllowedApp updateApp(long id, AllowedAppDto allowedAppDto) {
        AllowedApp allowedApp = allowedAppRepository.findById(id).orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "No such application"));
        allowedApp.setEnabled(allowedAppDto.isEnabled());
        allowedApp.setTokenValiditySeconds(allowedAppDto.getTokenValiditySeconds());
        allowedApp.setRefreshTokenValiditySeconds(allowedAppDto.getRefreshTokenValiditySeconds());
        return allowedAppRepository.save(allowedApp);
    }

    @Override
    public List<AllowedApp> getApps() {
        return allowedAppRepository.findAll();
    }

    @Override
    public AllowedApp getApp(long id) {
        return allowedAppRepository.findById(id).orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "No such application"));
    }

    @Override
    public void deleteApp(long id) {
        allowedAppRepository.deleteById(id);
    }
}