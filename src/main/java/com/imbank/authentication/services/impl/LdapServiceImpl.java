package com.imbank.authentication.services.impl;

import com.imbank.authentication.dtos.AuthDTO;
import com.imbank.authentication.dtos.LdapUserDTO;
import com.imbank.authentication.services.LdapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.stereotype.Service;

import javax.naming.directory.Attributes;

@Service
@Slf4j
public class LdapServiceImpl implements LdapService {
    @Value("${spring.ldap.urls}")
    private String[] ldapUrls;

    @Value("${spring.ldap.base}")
    private String ldapBaseDn;

    @Value("${spring.ldap.username}")
    private String ldapSecurityPrincipal;

    @Value("${spring.ldap.password}")
    private String ldapPrincipalPassword;

//    @Value("${spring.ldap.user.dn.pattern}")
//    private String ldapUserDnPattern;


    @Override
    public LdapUserDTO getADDetails(AuthDTO credentials) {
        try {
            LdapContextSource contextSource = new LdapContextSource();
            contextSource.setUrls(ldapUrls);
            contextSource.setUserDn(ldapSecurityPrincipal);
            contextSource.setPassword(ldapPrincipalPassword);
            contextSource.setDirObjectFactory(DefaultDirObjectFactory.class);
            contextSource.afterPropertiesSet();

            LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
            ldapTemplate.afterPropertiesSet();
            AndFilter filter = new AndFilter();
            filter.and(new EqualsFilter("sAMAccountName", credentials.getUsername()));
            boolean validCredentials = ldapTemplate.authenticate(ldapBaseDn, filter.encode(), credentials.getPassword());

            if (!validCredentials) {
                throw new IllegalArgumentException("Invalid username or password");
            }
            LdapUserDTO ldapUser = new LdapUserDTO();
            ContextMapper<Object> contextMapper = o -> {
                LdapServiceImpl.log.info("User found with valid credentials: {}", o);
                Attributes a = ((DirContextAdapter) o).getAttributes();

                ldapUser.setFirstName(getValue(a, "givenName"));
                ldapUser.setLastName(getValue(a, "sn"));
                ldapUser.setName(getValue(a, "name"));
                ldapUser.setEmail(getValue(a, "userPrincipalName"));

                return ldapUser;
            };
            ldapTemplate.search(ldapBaseDn, filter.encode(), contextMapper);
            return ldapUser;
        } catch (Exception e) {
            LdapServiceImpl.log.error("Could not authenticate", e);
        }
        return null;
    }

    @Override
    public LdapUserDTO getADDetails(String adUsername) {
        try {
            LdapContextSource contextSource = new LdapContextSource();
            contextSource.setUrls(ldapUrls);
            contextSource.setUserDn(ldapSecurityPrincipal);
            contextSource.setPassword(ldapPrincipalPassword);
            contextSource.setDirObjectFactory(DefaultDirObjectFactory.class);
            contextSource.afterPropertiesSet();

            LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
            ldapTemplate.afterPropertiesSet();
            AndFilter filter = new AndFilter();
            filter.and(new EqualsFilter("sAMAccountName", credentials.getUsername()));
            boolean validCredentials = ldapTemplate.authenticate(ldapBaseDn, filter.encode(), credentials.getPassword());

            if (!validCredentials) {
                throw new IllegalArgumentException("Invalid username or password");
            }
            LdapUserDTO ldapUser = new LdapUserDTO();
            ContextMapper<Object> contextMapper = o -> {
                LdapServiceImpl.log.info("User found with valid credentials: {}", o);
                Attributes a = ((DirContextAdapter) o).getAttributes();

                ldapUser.setFirstName(getValue(a, "givenName"));
                ldapUser.setLastName(getValue(a, "sn"));
                ldapUser.setName(getValue(a, "name"));
                ldapUser.setEmail(getValue(a, "userPrincipalName"));

                return ldapUser;
            };
            ldapTemplate.search(ldapBaseDn, filter.encode(), contextMapper);
            return ldapUser;
        } catch (Exception e) {
            LdapServiceImpl.log.error("Could not authenticate", e);
        }
        return null;
    }




    private String getValue(Attributes a, String key) {
        try {
            return "" + a.get(key).get();
        } catch (Exception e) {
            return "";
        }
    }

}
