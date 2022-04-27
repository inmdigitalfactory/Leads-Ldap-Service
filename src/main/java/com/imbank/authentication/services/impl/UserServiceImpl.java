package com.imbank.authentication.services.impl;

import com.imbank.authentication.dtos.LdapUserDTO;
import com.imbank.authentication.dtos.RoleDto;
import com.imbank.authentication.dtos.SystemAccessDto;
import com.imbank.authentication.dtos.UserDto;
import com.imbank.authentication.entities.AllowedApp;
import com.imbank.authentication.entities.Role;
import com.imbank.authentication.entities.SystemAccess;
import com.imbank.authentication.entities.User;
import com.imbank.authentication.enums.AppPermission;
import com.imbank.authentication.exceptions.AuthenticationExceptionImpl;
import com.imbank.authentication.repositories.AllowedAppRepository;
import com.imbank.authentication.repositories.RoleRepository;
import com.imbank.authentication.repositories.SystemAccessRepository;
import com.imbank.authentication.repositories.UserRepository;
import com.imbank.authentication.services.LdapService;
import com.imbank.authentication.services.UserService;
import com.imbank.authentication.utils.AuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private AllowedAppRepository allowedAppRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SystemAccessRepository systemAccessRepository;

    @Autowired
    private LdapService ldapService;

    @Value("${spring.ldap.base}")
    private String ldapBase;

    @Override
    public User createUser(UserDto userDto) {
        //see if the app exists
//        AllowedApp allowedApp = allowedAppRepository.findById(userDto.getAppId()).orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "Unknown Application"));
        AuthUtils.ensurePermitted((AllowedApp) null, List.of(AppPermission.createUser));

        //ensure the user has not already been added to the ldap service
        Optional<User> optionalUser = userRepository.findFirstByUsername(userDto.getUsername());
        if(optionalUser.isPresent()) {
            throw new AuthenticationExceptionImpl(HttpStatus.BAD_REQUEST, "User is already cr" +
                    "eated. Please proceed to add a system that this user can access");
        }

        //Ensure the user being created is in the the Active directory
        LdapUserDTO ldapUserDTO = ldapService.getADDetails(userDto.getUsername(), ldapBase);
        if(ldapUserDTO == null) {
            throw new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, String.format("No user found with username '%s' under the domain '%s'", userDto.getUsername(), ldapBase));
        }

//        //ensure the user has not already been added to this app
//        optionalUser = userRepository.findFirstByUsername(ldapUserDTO.getUsername());
//        if(optionalUser.isPresent() && optionalUser.get().getSystemAccesses().stream().anyMatch(systemAccess -> systemAccess.getApp().getId() == userDto.getAppId())) {
//            throw new AuthenticationExceptionImpl(HttpStatus.BAD_REQUEST, "User already has access to this service");
//        }

        //retrieve or create this user
        User user = optionalUser.orElse(
                User.builder()
                        .baseDn(ldapUserDTO.getBaseDn())
                        .enabled(userDto.isEnabled())
                        .name(ldapUserDTO.getName())
                        .firstName(ldapUserDTO.getFirstName())
                        .department(ldapUserDTO.getDepartment())
                        .lastName(ldapUserDTO.getLastName())
                        .email(ldapUserDTO.getEmail())
                        .phone(ldapUserDTO.getPhone())
                        .systemAccesses(new HashSet<>())
                        .username(ldapUserDTO.getUsername())
                        .build()
        );

//        List<SystemAccess> systemAccesses = userDto.getRoles().stream().map(roleDto -> {
//            Set<AllowedApp> apps = ObjectUtils.isEmpty(roleDto.getApps())
//                                        ? null
//                                        : new HashSet<>(allowedAppRepository.findAllById(roleDto.getApps()));
//            Role role = roleRepository.findById(roleDto.getRole()).orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "No such role"));
//            return SystemAccess.builder()
//                    .app(allowedApp)
//                    .apps(apps)
//                    .role(role)
//                    .build();
//        }).collect(Collectors.toList());
        //assign this app and role to the user
//        user.getSystemAccesses().addAll(systemAccessRepository.saveAll(systemAccesses));

        //save or update the user records
        user = userRepository.save(user);

        //this app does not use userManagement module. Add it since the app has started managing users
//        if(!allowedApp.getModules().contains(AuthModule.userManagement)) {
//            allowedApp.getModules().add(AuthModule.userManagement);
//            allowedAppRepository.save(allowedApp);
//        }
        return user;
    }

    @Override
    public User updateUser(long id, UserDto userDto) {
        User user = userRepository.findById(id).orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "Unknown user"));
        AuthUtils.ensurePermitted(user.getSystemAccesses(), List.of(AppPermission.updateUser));
        user.setEnabled(userDto.isEnabled());
        user.setBaseDn(user.getBaseDn());

        user = userRepository.save(user);
        return user;
    }

    @Override
    public List<User> getUsers() {
        AuthUtils.ensurePermitted((AllowedApp) null, List.of(AppPermission.getAllUsers));
        return userRepository.findAll();
    }

    @Override
    public List<User> getAppUsers(long appId) {
        AllowedApp allowedApp = allowedAppRepository.findById(appId).orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "Unknown Application"));
        AuthUtils.ensurePermitted(allowedApp, List.of(AppPermission.getAllUsers));
//        return userRepository.findAllByApp(allowedApp);
        return null;
    }

    @Override
    public User getUser(long id) {
        User user = userRepository.findById(id).orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "Unknown user"));
        AuthUtils.ensurePermitted(user.getSystemAccesses(), List.of(AppPermission.getUser));
        return user;
    }

    @Override
    public void deleteUser(long id) {
        User user = userRepository.findById(id).orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "Unknown user"));
        AuthUtils.ensurePermitted((AllowedApp) null, List.of(AppPermission.deleteUser));
        userRepository.delete(user);
    }

    @Override
    public User updateUserRoles(Long userId, Long appId, RoleDto roleDto) {
        User user = userRepository.findById(userId).orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "Unknown user"));
        AuthUtils.ensurePermitted(user.getSystemAccesses(), List.of(AppPermission.updateUser));

        AllowedApp app = allowedAppRepository.findById(appId).orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "Unknown application"));
        Role role = roleRepository.findById(roleDto.getRole()).orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "No such role"));
        Optional<SystemAccess> existingAccess = systemAccessRepository.findFirstByUserAndApp(user, app);
        SystemAccess systemAccess = existingAccess.orElseGet(() -> SystemAccess.builder()
                .app(app)
//                    .apps(apps)
                .user(user)
                .role(role)
                .build());

        systemAccess.setRole(role);
        //assign this app and role to the user
        user.getSystemAccesses().add(systemAccessRepository.save(systemAccess));
        user.setModifiedOn(new Date());
        return userRepository.save(user);
    }

    @Override
    public User addSystemAccess(Long userId, SystemAccessDto systemAccessDto) {
        User user = userRepository.findById(userId).orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "Unknown user"));
        AuthUtils.ensurePermitted(systemAccessDto.getApp(), List.of(AppPermission.updateUser));

        AllowedApp app = allowedAppRepository.findById(systemAccessDto.getApp()).orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "Unknown application"));
        Role role = roleRepository.findById(systemAccessDto.getRole()).orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.NOT_FOUND, "No such role"));
        Optional<SystemAccess> existingAccess = systemAccessRepository.findFirstByUserAndApp(user, app);
        SystemAccess access = existingAccess.orElseGet(() -> SystemAccess.builder()
                .app(app)
//                    .apps(apps)
                .user(user)
                .role(role)
                .build());
        //assign this app and role to the user
        user.getSystemAccesses().add(systemAccessRepository.save(access));
        user.setModifiedOn(new Date());
        return userRepository.save(user);
    }

    @Override
    public LdapUserDTO searchUser(String username) {
        AuthUtils.ensurePermitted((AllowedApp) null, List.of(AppPermission.searchUser, AppPermission.createUser));
        return ldapService.getADDetails(username, ldapBase);
    }
}
