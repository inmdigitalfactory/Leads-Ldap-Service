package com.imbank.authentication.config;

import com.imbank.authentication.entities.*;
import com.imbank.authentication.enums.AppPermission;
import com.imbank.authentication.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.imbank.authentication.utils.Constants.APP_NAME;

@Component
public class Seeder {

    @Autowired
    private AllowedAppRepository allowedAppRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SystemAccessRepository systemAccessRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionRepository permissionRepository;


    @Bean
    @Profile({"dev", "uat"})
    public CommandLineRunner seedAllowedApps() {
        return args -> {
            if(allowedAppRepository.count() == 0) {
                AllowedApp allowedApp = new AllowedApp();
                allowedApp.setName("Test App");
                allowedApp.setAccessToken("TestToken");
                allowedApp.setTokenValiditySeconds(60*30);//30mins
                allowedApp.setRefreshTokenValiditySeconds(7200);//2hrs
                allowedApp.setEnabled(true);
                allowedAppRepository.save(allowedApp);
            }
        };
    }

    @Bean
    public CommandLineRunner initializeApp() {
        return args -> {
            Optional<AllowedApp> optionalAllowedApp = allowedAppRepository.findFirstByName(APP_NAME);
            AllowedApp app = null;
            if(optionalAllowedApp.isEmpty()) {
                AllowedApp allowedApp = new AllowedApp();
                allowedApp.setName(APP_NAME);
                allowedApp.setAccessToken(UUID.randomUUID().toString());
                allowedApp.setTokenValiditySeconds(60*30);//30mins
                allowedApp.setRefreshTokenValiditySeconds(7200);//2hrs
                allowedApp.setEnabled(true);
                app = allowedAppRepository.save(allowedApp);
            }
            else {
                app = optionalAllowedApp.get();
            }
            String adminRoleName = "Admin";
            Role adminRole = roleRepository.findFirstByNameAndApp(adminRoleName, app);
            Set<Permission> allPermissions = new HashSet<>(permissionRepository.findAll());
            if(adminRole == null) {
                adminRole = new Role();
                adminRole.setApp(app);
                adminRole.setName(adminRoleName);
                adminRole = roleRepository.save(adminRole);
            }

            if(permissionRepository.count() == 0) {
                AllowedApp finalApp = app;
                allPermissions = new HashSet<>(permissionRepository.saveAll(Arrays.stream(AppPermission.values()).map(p -> {
                    Permission perm = new Permission();
                    perm.setApp(finalApp);
                    perm.setCode(p.name());
                    return perm;
                }).collect(Collectors.toSet())));
            }
            if(ObjectUtils.isEmpty(adminRole.getPermissions())) {
                adminRole.setPermissions(allPermissions);
                adminRole = roleRepository.save(adminRole);
            }


            String username = "einstein";
            Optional<User> adminUserOptional = userRepository.findFirstByUsername(username);
            User adminUser;
            if(adminUserOptional.isEmpty()) {
                adminUser = User.builder().username(username).baseDn("dc=example,dc=com").build();

            }
            else {
                adminUser = adminUserOptional.get();
            }
            adminUser = userRepository.save(adminUser);
            if(ObjectUtils.isEmpty(adminUser.getSystemAccesses())) {
                SystemAccess systemAccess = systemAccessRepository
                        .save(SystemAccess.builder()
                            .role(adminRole)
                                .user(adminUser)
                            .app(app)
                            .build()
                );
//                adminUser.setSystemAccesses(Set.of(systemAccess));
            }
        };
    }
}
