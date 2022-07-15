package com.imbank.authentication.services.impl;

import com.imbank.authentication.dtos.AppRoleDto;
import com.imbank.authentication.entities.AllowedApp;
import com.imbank.authentication.entities.Permission;
import com.imbank.authentication.entities.Role;
import com.imbank.authentication.enums.AppPermission;
import com.imbank.authentication.exceptions.AuthenticationExceptionImpl;
import com.imbank.authentication.repositories.AllowedAppRepository;
import com.imbank.authentication.repositories.PermissionRepository;
import com.imbank.authentication.repositories.RoleRepository;
import com.imbank.authentication.repositories.SystemAccessRepository;
import com.imbank.authentication.services.RoleService;
import com.imbank.authentication.utils.AuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AllowedAppRepository allowedAppRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private SystemAccessRepository systemAccessRepository;

    @Override
    public List<Role> getAppRoles(Long appId) {
        AuthUtils.ensurePermitted(appId, List.of(AppPermission.getRoles));
        return roleRepository.findAllByAppId(appId);
    }

    @Override
    public Role addAppRole(Long appId, AppRoleDto roleDto) {
        AuthUtils.ensurePermitted(appId, List.of(AppPermission.addRole));
        AllowedApp app = allowedAppRepository.findById(appId).orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "Unknown application"));
        Role existingRoleWithName = roleRepository.findFirstByAppAndNameIgnoreCase(app, roleDto.getName());
        if(existingRoleWithName != null) {
            throw new AuthenticationExceptionImpl(HttpStatus.BAD_REQUEST, "A role with the specified name already exists");
        }
        Role role = new Role();
        role.setApp(app);
        role.setDescription(roleDto.getDescription());
        role.setName(roleDto.getName());
        return roleRepository.save(role);
    }

    @Override
    public Role updateRole(Long roleId, AppRoleDto roleDto) {
        Role role = roleRepository.findById(roleId).orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "Unknown role"));
        AllowedApp app = role.getApp();
        Role existingRoleWithName = roleRepository.findFirstByAppAndNameIgnoreCase(app , roleDto.getName());
        if(existingRoleWithName != null && !Objects.equals(role.getId(), existingRoleWithName.getId())) {
            throw new AuthenticationExceptionImpl(HttpStatus.BAD_REQUEST, "A role with the specified name already exists");
        }
        if(!ObjectUtils.isEmpty(roleDto.getName())){
            role.setName(roleDto.getName());
        }
        AuthUtils.ensurePermitted(app, List.of(AppPermission.updateRole));
        role.setApp(app);
        role.setDescription(roleDto.getDescription());
        return roleRepository.save(role);
    }

    @Override
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId).orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "Unknown role"));
        AllowedApp app = role.getApp();
        AuthUtils.ensurePermitted(app, List.of(AppPermission.deleteRole));
        //unassign this role to every user
        systemAccessRepository.deleteAllByRole(role);
        roleRepository.delete(role);
    }

    @Override
    public Role setPermissions(Long roleId, List<Long> permissions) {
        Role role = roleRepository.findById(roleId).orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "Unknown role"));
        AllowedApp app = role.getApp();
        AuthUtils.ensurePermitted(app, List.of(AppPermission.setRolePermissions));
        List<Permission> permissionList = permissionRepository.findAllById(permissions);
        role.setPermissions(null);
        role = roleRepository.save(role);
        role.setPermissions(new HashSet<>(permissionList));
        log.info("Permissions are {}", permissionList);
        return roleRepository.save(role);
    }

    @Override
    public Role addPermission(Long roleId, Long permission) {
        Role role = roleRepository.findById(roleId).orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "Unknown role"));
        Permission permissionToAdd = permissionRepository.findById(permission).orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "Unknown permission"));
        AllowedApp app = role.getApp();
        AuthUtils.ensurePermitted(app, List.of(AppPermission.setRolePermissions));
        role.getPermissions().add(permissionToAdd);
        return roleRepository.save(role);
    }

    @Override
    public Role removePermission(Long roleId, Long permission) {
        Role role = roleRepository.findById(roleId).orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "Unknown role"));
        Permission permissionToRemove = permissionRepository.findById(permission).orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "Unknown permission"));
        AllowedApp app = role.getApp();
        AuthUtils.ensurePermitted(app, List.of(AppPermission.setRolePermissions));
        role.getPermissions().remove(permissionToRemove);
        return roleRepository.save(role);
    }
}
