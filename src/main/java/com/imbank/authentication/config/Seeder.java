package com.imbank.authentication.config;

import com.imbank.authentication.entities.AllowedApp;
import com.imbank.authentication.repositories.AllowedAppRepository;
import com.imbank.authentication.repositories.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class Seeder {

    @Autowired
    private AllowedAppRepository allowedAppRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Bean
    public CommandLineRunner seedAllowedApps() {
        return args -> {
            if(allowedAppRepository.count() == 0) {
                AllowedApp allowedApp = new AllowedApp();
                allowedApp.setName("Test App");
                allowedApp.setAccessToken("TestToken");
                allowedApp.setEnabled(true);
                allowedAppRepository.save(allowedApp);
            }
        };
    }

    @Bean
    public CommandLineRunner seedPermissions() {
        return args -> {
            if(permissionRepository.count() == 0) {
                AllowedApp allowedApp = new AllowedApp();
                allowedApp.setName("Test App");
                allowedApp.setAccessToken("TestToken");
                allowedApp.setEnabled(true);
                allowedAppRepository.save(allowedApp);
            }
        };
    }
}
