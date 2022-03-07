package com.imbank.authentication.config;

import com.imbank.authentication.utils.RequestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class BeanCreator {

    @Bean
    public void setupSecureLdap() {
        RequestUtils.setupTrustStore();
    }

}
