package com.imbank.authentication.services;

import com.imbank.authentication.dtos.AppRoleDto;
import com.imbank.authentication.entities.Role;

import java.util.List;

public interface RoleService {
    List<Role> getAppRoles(Long appId);

    Role addAppRole(Long appId, AppRoleDto roleDto);

    Role updateRole(Long roleId, AppRoleDto roleDto);

    void deleteRole(Long roleId);

    Role setPermissions(Long roleId, List<Long> permissions);

    Role addPermission(Long roleId, Long permission);

    Role removePermission(Long roleId, Long permission);
}
