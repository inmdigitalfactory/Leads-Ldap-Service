package com.imbank.authentication.controllers;

import com.imbank.authentication.dtos.UserDto;
import com.imbank.authentication.entities.User;
import com.imbank.authentication.services.UserService;
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

    @PostMapping("{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody @Valid UserDto allowedUserDto) {
        return ResponseEntity.ok().body(userService.updateUser(id, allowedUserDto));
    }

    @GetMapping("")
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok().body(userService.getUsers());
    }

    @GetMapping("{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return ResponseEntity.ok().body(userService.getUser(id));
    }

    @GetMapping("{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().body("Deleted");
    }
}
