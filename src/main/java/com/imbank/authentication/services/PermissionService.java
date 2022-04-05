package com.imbank.authentication.services;

import com.imbank.authentication.dtos.PermissionDto;
import com.imbank.authentication.entities.Permission;

import java.util.List;

public interface PermissionService {
    
    List<Permission> getAppPermissions(Long appId);

    Permission addAppPermission(Long appId, PermissionDto permissionDto);

    Permission updatePermission(Long permissionId, PermissionDto permissionDto);

    void deletePermission(Long permissionId);
}
