package com.imbank.authentication.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imbank.authentication.dtos.PagerDto;
import com.imbank.authentication.entities.AllowedApp;
import com.imbank.authentication.entities.AuditLog;
import com.imbank.authentication.entities.SystemAccess;
import com.imbank.authentication.entities.User;
import com.imbank.authentication.enums.AuditAction;
import com.imbank.authentication.exceptions.AuthenticationExceptionImpl;
import com.imbank.authentication.repositories.AuditRepository;
import com.imbank.authentication.repositories.UserRepository;
import com.imbank.authentication.services.AuditLogService;
import com.imbank.authentication.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Page<AuditLog> getAuditLogs(PagerDto pager) {
        //TODO uncomment below
        //AuthUtils.ensurePermitted((AllowedApp) null, List.of(AppPermission.viewAuditLogs));
        Pageable pageRequest;
        if (!ObjectUtils.isEmpty(pager.getSortBy())) {
            pageRequest = PageRequest.of(pager.getPage(), pager.getPageSize(), pager.getSortOrder(), pager.getSortBy());
        } else {
            pageRequest = PageRequest.of(pager.getPage(), pager.getPageSize(), pager.getSortOrder());
        }

        return auditRepository.findAll(pageRequest);
    }

    @Override
    public List<AuditLog> getAuditLogs() {
        //TODO uncomment below
        //AuthUtils.ensurePermitted((AllowedApp) null, List.of(AppPermission.viewAuditLogs));
        return auditRepository.findAllByOrderByIdDesc();
    }

    @Override
    public Page<AuditLog> getAuditLogsByApp(long appId, PagerDto pager) {
        //TODO uncomment below
        //AuthUtils.ensurePermitted((AllowedApp) null, List.of(AppPermission.viewAuditLogs));
        Pageable pageRequest;
        if (!ObjectUtils.isEmpty(pager.getSortBy())) {
            pageRequest = PageRequest.of(pager.getPage(), pager.getPageSize(), pager.getSortOrder(), pager.getSortBy());
        } else {
            pageRequest = PageRequest.of(pager.getPage(), pager.getPageSize(), pager.getSortOrder());
        }
        return auditRepository.findAllByAppId(appId, pageRequest);
    }

    @Override
    public Page<AuditLog> getAuditLogsByUser(long userId, PagerDto pager) {
        //TODO uncomment below
        //AuthUtils.ensurePermitted((AllowedApp) null, List.of(AppPermission.viewAuditLogs));
        Pageable pageRequest;
        if (!ObjectUtils.isEmpty(pager.getSortBy())) {
            pageRequest = PageRequest.of(pager.getPage(), pager.getPageSize(), pager.getSortOrder(), pager.getSortBy());
        } else {
            pageRequest = PageRequest.of(pager.getPage(), pager.getPageSize(), pager.getSortOrder());
        }
        return auditRepository.findAllByUserId(userId, pageRequest);
    }

    @Override
    public AuditLog createAuditLog(AuditAction action, AllowedApp app, User user) {
        return createAuditLog(action, app, user, null);
    }

    @Override
    public AuditLog createAuditLog(AuditAction action, AllowedApp app, User user, Map<String, Object> metadata) {
        AuditLog.AuditLogBuilder auditLogBuilder = AuditLog.builder()
                .action(action);
        if (!ObjectUtils.isEmpty(app)) {
            auditLogBuilder = auditLogBuilder.appName(app.getName()).appId(app.getId());
        }
        if (!ObjectUtils.isEmpty(user)) {
            auditLogBuilder = auditLogBuilder.userName(user.getUsername()).userId(user.getId());
        }
        if (!ObjectUtils.isEmpty(metadata)) {
            try {
                auditLogBuilder = auditLogBuilder.metadata(objectMapper.writeValueAsString(metadata));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        String username = AuthUtils.getLoggedInUser().orElseThrow(() -> new RuntimeException("You must be logged in to perform this action"));
        User auditingUser = userRepository.findFirstByUsernameIgnoreCase(username).orElseThrow(()->new AuthenticationExceptionImpl(HttpStatus.FORBIDDEN, "You must be logged in to perform thsi action"));
        SystemAccess systemAccess = auditingUser.getSystemAccesses().stream().filter(sa -> sa.getApp().isLdapService()).findFirst().orElseThrow();
        auditLogBuilder = auditLogBuilder.role(systemAccess.getRole().getName());
        AuditLog auditLog = auditLogBuilder.build();
        return auditRepository.save(auditLog);
    }
}
