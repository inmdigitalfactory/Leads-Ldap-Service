package com.imbank.authentication.services;

import com.imbank.authentication.dtos.LdapUserDTO;
import com.imbank.authentication.dtos.RoleDto;
import com.imbank.authentication.dtos.SystemAccessDto;
import com.imbank.authentication.dtos.UserDto;
import com.imbank.authentication.entities.User;

import java.util.List;

public interface UserService {
    User createUser(UserDto user);
    User updateUser(long id, UserDto user);
    List<User> getUsers();

    List<User> getAppUsers(long appId);

    User getUser(long id);
    void deleteUser(long id);

    User updateUserRoles(Long userId, Long appId, RoleDto roleDto);
    LdapUserDTO searchUser(String username);


    User addSystemAccess(Long userId, SystemAccessDto systemAccessDto);

    User updateUserStatus(Long userId, Long appId);
}
