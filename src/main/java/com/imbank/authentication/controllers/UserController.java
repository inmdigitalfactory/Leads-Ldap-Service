package com.imbank.authentication.controllers;

import com.imbank.authentication.dtos.LdapUserDTO;
import com.imbank.authentication.dtos.RoleDto;
import com.imbank.authentication.dtos.SystemAccessDto;
import com.imbank.authentication.dtos.UserDto;
import com.imbank.authentication.entities.User;
import com.imbank.authentication.services.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("")
    public ResponseEntity<User> create(@RequestBody @Valid UserDto allowedUserDto) {
        return ResponseEntity.ok().body(userService.createUser(allowedUserDto));
    }

    @PutMapping("{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody @Valid UserDto allowedUserDto) {
        return ResponseEntity.ok().body(userService.updateUser(id, allowedUserDto));
    }

    @PutMapping("{userId}/system-accesses/{appId}/roles")
    public ResponseEntity<User> updateRoles(@PathVariable Long userId, @PathVariable Long appId, @RequestBody @Valid RoleDto roleDto) {
        return ResponseEntity.ok().body(userService.updateUserRoles(userId, appId, roleDto));
    }

    @PostMapping("{userId}/system-accesses")
    public ResponseEntity<User> addSystemAccess(@PathVariable Long userId, @RequestBody @Valid SystemAccessDto systemAccessDto) {
        return ResponseEntity.ok().body(userService.addSystemAccess(userId, systemAccessDto));
    }

    @GetMapping("")
    @SecurityRequirement(name = "Application Access Token")
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok().body(userService.getUsers());
    }

    @GetMapping("{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return ResponseEntity.ok().body(userService.getUser(id));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().body("Deleted");
    }

    @GetMapping("search/{username}")
    public ResponseEntity<LdapUserDTO> searchUser(@PathVariable String username) {
        return ResponseEntity.ok().body(userService.searchUser(username));
    }
}
