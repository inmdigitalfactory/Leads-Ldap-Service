package com.imbank.authentication.controllers;


import com.imbank.authentication.dtos.PagerDto;
import com.imbank.authentication.entities.AuditLog;
import com.imbank.authentication.services.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("audit-logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(PagerDto pagerDto) {
        return ResponseEntity.ok().body(auditLogService.getAuditLogs(pagerDto));
    }

    @GetMapping("/apps/{appId}")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByApp(@PathVariable long appId, PagerDto pagerDto) {
        return ResponseEntity.ok().body(auditLogService.getAuditLogsByApp(appId, pagerDto));
    }

    @GetMapping("users/{userId}")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByUser(@PathVariable long userId, PagerDto pagerDto) {
        return ResponseEntity.ok().body(auditLogService.getAuditLogsByUser(userId, pagerDto));
    }

}
