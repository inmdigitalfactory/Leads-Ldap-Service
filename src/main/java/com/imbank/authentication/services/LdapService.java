package com.imbank.authentication.services;

import com.imbank.authentication.dtos.AuthDTO;
import com.imbank.authentication.dtos.LdapUserDTO;
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
public class LdapService {
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


    public LdapUserDTO getADDetails(AuthDTO credentials) {
        try {
            LdapContextSource contextSource = new LdapContextSource();
            contextSource.setUrls(ldapUrls);
            contextSource.setUserDn(ldapSecurityPrincipal);
            contextSource.setPassword(ldapPrincipalPassword);
            contextSource.setDirObjectFactory(DefaultDirObjectFactory.class);
            contextSource.afterPropertiesSet();

            LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
            ldapTemplate.getContextSource().getContext("uid="+credentials.getUsername()+","+ldapBaseDn, credentials.getPassword());

            AndFilter filter = new AndFilter();
            filter.and(new EqualsFilter("sAMAccountName", credentials.getUsername()));
            LdapUserDTO ldapUser = new LdapUserDTO();
            ContextMapper<Object> contextMapper = o -> {
                log.info("User found with valid credentials: {}", o);
                Attributes a = ((DirContextAdapter) o).getAttributes();

                ldapUser.setFirstName(getValue(a, "givenName"));
                ldapUser.setLastName(getValue(a, "sn"));
                ldapUser.setUsername(getValue(a, "uid"));
                ldapUser.setName(getValue(a, "cn"));
                ldapUser.setPhone(getValue(a, "telephoneNumber"));
                ldapUser.setEmail(getValue(a, "mail"));

                return ldapUser;
            };
            ldapTemplate.search(ldapBaseDn, filter.encode(), contextMapper);
            return ldapUser;
        } catch (Exception e) {
            log.error("Could not authenticate", e);
        }
        return null;
    }

    private String getValue(Attributes a, String key) {
        try{
            return ""+a.get(key).get();
        }
        catch (Exception e) {
            return "";
        }
    }

}
