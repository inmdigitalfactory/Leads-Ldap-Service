package com.imbank.authentication.config;

import com.imbank.authentication.utils.RequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.opensaml.security.x509.X509Support;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.stereotype.Component;

import java.io.File;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

@Component
@Slf4j
public class BeanCreator {

    @Value("${spring.ldap.urls}")
    private String[] ldapUrls;

    @Value("${spring.ldap.username}")
    private String ldapSecurityPrincipal;

    @Value("${spring.ldap.password}")
    private String ldapPrincipalPassword;

    @Bean("keystore")
    @Profile({"dev", "uat"})
    public KeyStore setupSecureLdap() {
        log.info("=========================================DEV");
        return RequestUtils.setupTrustStore("ldapserver.cer");
    }

    @Bean("keystore")
    @Profile({"prod"})
    public KeyStore setupSecureLdapProd() {
        log.info("=========================================PROD");
        return RequestUtils.setupTrustStore("ldapserver.prod.cer");
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


    @Bean
    public RelyingPartyRegistrationRepository relyingPartyRegistrations() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File verificationKey = new File(classLoader.getResource("saml.crt").getFile());
        X509Certificate certificate = X509Support.decodeCertificate(verificationKey);
        Saml2X509Credential credential = Saml2X509Credential.verification(certificate);
        RelyingPartyRegistration registration = RelyingPartyRegistration
                .withRegistrationId("okta-saml")
                .assertingPartyDetails(party -> party
                        .entityId("http://www.okta.com/exksqq6qjKMFpEAfz696")
                        .singleSignOnServiceLocation("https://trial-5696973.okta.com/app/trial-5696973_imbankldapservice_1/exksqq6qjKMFpEAfz696/sso/saml")
                        .wantAuthnRequestsSigned(false)
                        .verificationX509Credentials(c -> c.add(credential))
                ).build();
        return new InMemoryRelyingPartyRegistrationRepository(registration);
    }

}
