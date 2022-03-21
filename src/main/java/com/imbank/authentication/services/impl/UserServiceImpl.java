package com.imbank.authentication.services.impl;

import com.imbank.authentication.dtos.UserDto;
import com.imbank.authentication.entities.AllowedApp;
import com.imbank.authentication.entities.User;
import com.imbank.authentication.repositories.AllowedAppRepository;
import com.imbank.authentication.services.LdapService;
import com.imbank.authentication.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private AllowedAppRepository allowedAppRepository;

    @Autowired
    private LdapService ldapService;

    @Override
    public User createUser(UserDto user) {
        //see if the app exists
        AllowedApp allowedApp = allowedAppRepository.findById(user.getAppId()).orElseThrow();

        return null;
    }

    @Override
    public User updateUser(long id, UserDto user) {
        return null;
    }

    @Override
    public List<User> getUsers() {
        return null;
    }

    @Override
    public User getUser(long id) {
        return null;
    }

    @Override
    public void deleteUser(long id) {

    }
}
