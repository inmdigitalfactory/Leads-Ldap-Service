package com.imbank.authentication.config;

import com.imbank.authentication.utils.RequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BeanCreator {

    @Value("${spring.ldap.urls}")
    private String[] ldapUrls;

    @Value("${spring.ldap.username}")
    private String ldapSecurityPrincipal;

    @Value("${spring.ldap.password}")
    private String ldapPrincipalPassword;

    @Bean
    @Profile({"dev", "uat"})
    public void setupSecureLdap() {
        log.info("=========================================DEV");
        RequestUtils.setupTrustStore("ldapserver.cer");
    }

    @Bean
    @Profile({"prod"})
    public void setupSecureLdapProd() {
        log.info("=========================================PROD");
        RequestUtils.setupTrustStore("ldapserver.prod.cer");
    }


    @Bean
    public LdapTemplate ldapTemplate() throws Exception {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrls(ldapUrls);
        contextSource.setUserDn(ldapSecurityPrincipal);
        contextSource.setPassword(ldapPrincipalPassword);
        contextSource.setDirObjectFactory(DefaultDirObjectFactory.class);
        contextSource.setReferral("ignore");
        contextSource.afterPropertiesSet();

        LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
        ldapTemplate.afterPropertiesSet();
        return ldapTemplate;
    }

}
