package com.imbank.authentication.controllers;

import com.imbank.authentication.dtos.AppRoleDto;
import com.imbank.authentication.entities.Role;
import com.imbank.authentication.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping("{appId}")
    public ResponseEntity<List<Role>> getAppRoles(@PathVariable Long appId) {
        return ResponseEntity.ok(roleService.getAppRoles(appId));
    }

    @PostMapping("{appId}")
    public ResponseEntity<Role> addAppRole(@PathVariable Long appId, @RequestBody @Valid AppRoleDto roleDto) {
        return ResponseEntity.ok(roleService.addAppRole(appId, roleDto));
    }

    @PutMapping("{roleId}")
    public ResponseEntity<Role> updateRole(@PathVariable Long roleId, @RequestBody AppRoleDto roleDto) {
        return ResponseEntity.ok(roleService.updateRole(roleId, roleDto));
    }

    @PutMapping("{roleId}/permissions")
    public ResponseEntity<Role> addPermissions(@PathVariable Long roleId, @RequestBody List<Long> permissions) {
        return ResponseEntity.ok(roleService.setPermissions(roleId, permissions));
    }

    @PutMapping("{roleId}/{permissionId}")
    public ResponseEntity<Role> addPermission(@PathVariable Long roleId, @PathVariable Long permissionId) {
        return ResponseEntity.ok(roleService.addPermission(roleId, permissionId));
    }


    @DeleteMapping("{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return ResponseEntity.ok().build();
    }

}
