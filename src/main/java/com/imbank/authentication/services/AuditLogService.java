package com.imbank.authentication.services;

import com.imbank.authentication.dtos.PagerDto;
import com.imbank.authentication.entities.AllowedApp;
import com.imbank.authentication.entities.AuditLog;
import com.imbank.authentication.entities.User;
import com.imbank.authentication.enums.AuditAction;
import org.springframework.data.domain.Page;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface AuditLogService {

    Page<AuditLog> getAuditLogs(PagerDto pager);
    List<AuditLog> getAuditLogs();
    Page<AuditLog> getAuditLogsByApp(long appId, PagerDto pager);
    Page<AuditLog> getAuditLogsByUser(long userId, PagerDto pager);
    AuditLog createAuditLog(AuditAction addUser, AllowedApp app, User user);

    AuditLog createAuditLog(AuditAction addUser, AllowedApp o, User user, Map<String, Object> metadata);

    ByteArrayInputStream downloadLogs(Date startDate, Date endDate);
}
