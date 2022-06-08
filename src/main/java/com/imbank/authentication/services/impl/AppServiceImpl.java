package com.imbank.authentication.services.impl;

import com.imbank.authentication.dtos.AllowedAppDto;
import com.imbank.authentication.entities.AllowedApp;
import com.imbank.authentication.enums.AppPermission;
import com.imbank.authentication.enums.AuditAction;
import com.imbank.authentication.exceptions.AuthenticationExceptionImpl;
import com.imbank.authentication.repositories.AllowedAppRepository;
import com.imbank.authentication.repositories.PermissionRepository;
import com.imbank.authentication.repositories.RoleRepository;
import com.imbank.authentication.repositories.SystemAccessRepository;
import com.imbank.authentication.services.AppService;
import com.imbank.authentication.services.AuditLogService;
import com.imbank.authentication.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AppServiceImpl implements AppService {

    @Autowired
    private AllowedAppRepository allowedAppRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private SystemAccessRepository systemAccessRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    public AllowedApp createApp(AllowedAppDto allowedAppDto) {
        AuthUtils.ensurePermitted((AllowedApp) null, List.of(AppPermission.createApp));
        if(allowedAppRepository.findFirstByNameIgnoreCase(allowedAppDto.getName()).isPresent()) {
            throw new AuthenticationExceptionImpl(HttpStatus.BAD_REQUEST,"App already exists");
        }
        AllowedApp allowedApp = new AllowedApp();
        allowedApp.setName(allowedAppDto.getName());
        allowedApp.setRefreshTokenValiditySeconds(allowedAppDto.getRefreshTokenValiditySeconds());
        String accessToken = AuthUtils.generateAccessToken();
        allowedApp.setAccessToken(accessToken);
        allowedApp.setTokenValiditySeconds(allowedAppDto.getTokenValiditySeconds());
        allowedApp.setEnabled(allowedAppDto.getEnabled());
        allowedApp = allowedAppRepository.save(allowedApp);

        auditLogService.createAuditLog(AuditAction.createApp, allowedApp, null);
        return allowedApp;

    }

    @Override
    public String resetToken(long id) {
        AllowedApp allowedApp = allowedAppRepository.findById(id)
                .orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "No such application"));
        AuthUtils.ensurePermitted(allowedApp, List.of(AppPermission.resetAppToken));

        String accessToken = AuthUtils.generateAccessToken();
        allowedApp.setAccessToken(accessToken);
        allowedAppRepository.save(allowedApp);
        return accessToken;
    }

    @Override
    public AllowedApp updateApp(long id, AllowedAppDto allowedAppDto) {
        AllowedApp allowedApp = allowedAppRepository.findById(id)
                .orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "No such application"));
        AuthUtils.ensurePermitted(allowedApp, List.of(AppPermission.updateApp));

        if(!allowedAppDto.getName().equals(allowedApp.getName()) && allowedAppRepository.findFirstByNameIgnoreCase(allowedAppDto.getName()).isPresent()) {
            throw new AuthenticationExceptionImpl(HttpStatus.BAD_REQUEST,"App already exists");
        }
        auditLogService.createAuditLog(AuditAction.updateApp, allowedApp, null, Map.of("enabled", allowedAppDto.getEnabled(), "tokenValiditySeconds", allowedAppDto.getTokenValiditySeconds(), "refreshTokenValiditySeconds", allowedAppDto.getRefreshTokenValiditySeconds(), "name", allowedAppDto.getName()));

        allowedApp.setEnabled(allowedAppDto.getEnabled());
        allowedApp.setName(allowedAppDto.getName());
        allowedApp.setTokenValiditySeconds(allowedAppDto.getTokenValiditySeconds());
        allowedApp.setRefreshTokenValiditySeconds(allowedAppDto.getRefreshTokenValiditySeconds());
        return allowedAppRepository.save(allowedApp);
    }

    @Override
    public List<AllowedApp> getApps() {
        AuthUtils.ensurePermitted((AllowedApp) null, List.of(AppPermission.getAllApps));
        return allowedAppRepository.findAll();
    }

    @Override
    public AllowedApp getApp(long id) {
        AuthUtils.ensurePermitted(id, List.of(AppPermission.getApp));
        return allowedAppRepository.findById(id)
                .orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "No such application"));
    }

    @Override
    public void deleteApp(long id) {
        AuthUtils.ensurePermitted((AllowedApp) null, List.of(AppPermission.createApp));
        AllowedApp app = allowedAppRepository.findById(id).orElseThrow();

        auditLogService.createAuditLog(AuditAction.deleteApp, app, null);
        systemAccessRepository.deleteAllByAppId(id);
        permissionRepository.deleteAllByAppId(id);
        roleRepository.deleteAllByAppId(id);
        allowedAppRepository.deleteById(id);
    }
}
