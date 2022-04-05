package com.imbank.authentication.controllers;

import com.imbank.authentication.dtos.PermissionDto;
import com.imbank.authentication.entities.Permission;
import com.imbank.authentication.services.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @GetMapping("{appId}")
    public ResponseEntity<List<Permission>> getAppPermissions(@PathVariable Long appId) {
        return ResponseEntity.ok(permissionService.getAppPermissions(appId));
    }

    @PostMapping("{appId}")
    public ResponseEntity<Permission> addAppPermission(@PathVariable Long appId, @RequestBody PermissionDto permissionDto) {
        return ResponseEntity.ok(permissionService.addAppPermission(appId, permissionDto));
    }

    @PutMapping("{permissionId}")
    public ResponseEntity<Permission> updatePermission(@PathVariable Long permissionId, @RequestBody PermissionDto permissionDto) {
        return ResponseEntity.ok(permissionService.updatePermission(permissionId, permissionDto));
    }


    @DeleteMapping("{permissionId}")
    public ResponseEntity<Void> deletePermission(@PathVariable Long permissionId) {
        permissionService.deletePermission(permissionId);
        return ResponseEntity.ok().build();
    }

}
