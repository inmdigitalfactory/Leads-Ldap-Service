package com.imbank.authentication.config;

import com.imbank.authentication.entities.AllowedApp;
import com.imbank.authentication.repositories.AllowedAppRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
public class Seeder {

    @Autowired
    private AllowedAppRepository allowedAppRepository;


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
}
