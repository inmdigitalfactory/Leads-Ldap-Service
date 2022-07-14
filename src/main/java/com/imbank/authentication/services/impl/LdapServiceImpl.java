package com.imbank.authentication.services.impl;

import com.imbank.authentication.dtos.AuthDTO;
import com.imbank.authentication.dtos.LdapUserDTO;
import com.imbank.authentication.entities.AllowedApp;
import com.imbank.authentication.entities.User;
import com.imbank.authentication.enums.AuthModule;
import com.imbank.authentication.exceptions.AuthenticationExceptionImpl;
import com.imbank.authentication.repositories.UserRepository;
import com.imbank.authentication.services.LdapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LdapServiceImpl implements LdapService {

    @Value("${spring.ldap.baseKe}")
    private String ldapBaseDn;

    @Value("${spring.ldap.base}")
    private String baseDn;

    @Value("${spring.ldap.baseTz}")
    private String ldapBaseDnTzUsers;

    @Value("${spring.ldap.user.dn.pattern}")
    private String dnPattern;

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private LdapTemplate ldapTemplate;

    @Override
    public LdapUserDTO getADDetails(AllowedApp allowedApp, AuthDTO credentials) {

        if(allowedApp.getModules().contains(AuthModule.userManagement)) {
            //This app has enabled user management. This user must be added to authenticate
            User user = userRepository.findFirstByUsernameIgnoreCaseAndEnabled(credentials.getUsername(), true)
                    .orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.UNAUTHORIZED, "You are not authorized to access this service"));
            user.getSystemAccesses().stream()
                    .filter(systemAccess1 -> systemAccess1.getApp().getId().equals(allowedApp.getId()) && Boolean.TRUE.equals(systemAccess1.getEnabled()))
                    .findFirst()
                    .orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.FORBIDDEN, "You are forbidden from accessing this service"));
            LdapUserDTO ldapUserDTO = getADDetails(credentials.getUsername(), credentials.getPassword(), baseDn);
            if(!ObjectUtils.isEmpty(ldapUserDTO)) {
                if(!allowedApp.getModules().contains(AuthModule.roleManagement)) {
                    user.setSystemAccesses(null);
                }
                else if(!allowedApp.getModules().contains(AuthModule.permissionManagement)) {
                    user.setSystemAccesses(user.getSystemAccesses()
                            .stream()
                            .peek(systemAccess -> systemAccess.getRole().setPermissions(null))
                            .collect(Collectors.toSet()));
                }
                ldapUserDTO.setUser(user);
            }
            return ldapUserDTO;
        }

        //This app is not using the user management module. Check against KE and TZ
        //TODO, allow apps to specify which OUs they work with
        LdapUserDTO ldapUserDTO = getADDetails(credentials.getUsername(), credentials.getPassword(), ldapBaseDn);
        if(ObjectUtils.isEmpty(ldapUserDTO)) {
            ldapUserDTO = getADDetails(credentials.getUsername(), credentials.getPassword(), ldapBaseDnTzUsers);
        }
        return ldapUserDTO;
    }

    @Override
    public LdapUserDTO getADDetails(String adUsername, String baseDn) {
        return getADDetails(adUsername, null, baseDn);
    }

    @Override
    public LdapUserDTO getADDetailsByEmail(String email) {
        return getADDetails(email, null, baseDn, "mail");
    }

    private LdapUserDTO getADDetails(String adUsername, String password, String baseDn) {
        return getADDetails(adUsername, password, baseDn, dnPattern);
    }

    private LdapUserDTO getADDetails(String adUsername, String password, String baseDn, String dnPattern) {
        try {
            AndFilter filter = new AndFilter();
            filter.and(new EqualsFilter(dnPattern, adUsername));
            filter.and(new EqualsFilter("objectClass", "person"));
//            filter.and(new EqualsFilter("objectClass", "user"));
            filter.and(new EqualsFilter("objectClass", "top"));
            filter.and(new EqualsFilter("objectClass", "organizationalPerson"));

            LdapUserDTO ldapUser = new LdapUserDTO();
            LdapUserDTO finalLdapUser = ldapUser;
            ContextMapper<Object> contextMapper = o -> {
                log.info("User found with valid credentials: {}", o);
                Attributes a = ((DirContextAdapter) o).getAttributes();

                finalLdapUser.setFirstName(getValue(a, "givenName"));
                finalLdapUser.setLastName(getValue(a, "sn"));
                finalLdapUser.setName(getValue(a, "name"));
                finalLdapUser.setEmail(getValue(a, "mail"));
                finalLdapUser.setDepartment(getValue(a, "department"));
                finalLdapUser.setPhone(getValue(a, "phone"));
                finalLdapUser.setUsername(getValue(a, this.dnPattern));
                finalLdapUser.setDescription(getValue(a, "description"));
                finalLdapUser.setBaseDn(((DirContextAdapter) o).getDn() + "");

                if(ObjectUtils.isEmpty(finalLdapUser.getUsername())) {
                    finalLdapUser.setUsername(adUsername);
                }
                return finalLdapUser;
            };
            log.info("Searching for user {}. Filter: {}", adUsername, filter.encode());
            try {
                List<Object> results = ldapTemplate.search(baseDn, filter.encode(), SearchControls.SUBTREE_SCOPE, contextMapper);
                if(ObjectUtils.isEmpty(results)) {
                    ldapUser = null;
                }
            }
            catch (Exception e) {//Referrals might throw errors when using port 636. Consider using port 3269
                log.info("Some error searching for data: {}", e.getLocalizedMessage());
            }

            if(!ObjectUtils.isEmpty(password)) {//verify password as well
                log.info("Attempting to authenticate: {}", ldapUser);
                boolean validCredentials = !ObjectUtils.isEmpty(ldapUser) && !ObjectUtils.isEmpty(ldapUser.getBaseDn()) && ldapTemplate.authenticate(ldapUser.getBaseDn(), filter.encode(), password);
                if(!validCredentials) {
                    throw new IllegalArgumentException("Invalid username or password");
                }
            }

            return ldapUser;
        } catch (Exception e) {
            log.error("Could not authenticate", e);
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
