package com.imbank.authentication.services.impl;

import com.imbank.authentication.dtos.PermissionDto;
import com.imbank.authentication.entities.AllowedApp;
import com.imbank.authentication.entities.Permission;
import com.imbank.authentication.enums.AppPermission;
import com.imbank.authentication.exceptions.AuthenticationExceptionImpl;
import com.imbank.authentication.repositories.AllowedAppRepository;
import com.imbank.authentication.repositories.PermissionRepository;
import com.imbank.authentication.services.PermissionService;
import com.imbank.authentication.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private AllowedAppRepository allowedAppRepository;


    @Override
    public List<Permission> getAppPermissions(Long appId) {
        AuthUtils.ensurePermitted(appId, List.of(AppPermission.getPermissions));
        return permissionRepository.findAllByAppId(appId);
    }

    @Override
    public Permission addAppPermission(Long appId, PermissionDto permissionDto) {
        AuthUtils.ensurePermitted(appId, List.of(AppPermission.addPermission));
        AllowedApp app = allowedAppRepository.findById(appId).orElseThrow();
        Permission permission = new Permission();
        permission.setCode(permissionDto.getCode());
        permission.setDescription(permissionDto.getDescription());
        permission.setApp(app);
        return permissionRepository.save(permission);
    }

    @Override
    public Permission updatePermission(Long permissionId, PermissionDto permissionDto) {
        Permission permission = permissionRepository.findById(permissionId).orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "No such permission"));
        AuthUtils.ensurePermitted(permission.getApp(), List.of(AppPermission.addPermission));
        permission.setDescription(permissionDto.getDescription());
        permission.setCode(permissionDto.getCode());
        return permissionRepository.save(permission);
    }

    @Override
    public void deletePermission(Long permissionId) {
        Permission permission = permissionRepository.findById(permissionId).orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "No such permission"));
        AuthUtils.ensurePermitted(permission.getApp(), List.of(AppPermission.addPermission));
        permissionRepository.delete(permission);
    }
}
