package com.imbank.authentication.services;

import com.imbank.authentication.dtos.AllowedAppDto;
import com.imbank.authentication.dtos.UserDto;
import com.imbank.authentication.entities.User;

import java.util.List;

public interface UserService {
    User createUser(UserDto user);
    User updateUser(long id, UserDto user);
    List<User> getUsers();
    User getUser(long id);
    void deleteUser(long id);
}
