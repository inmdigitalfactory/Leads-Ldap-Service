package com.imbank.authentication.controllers;


import com.imbank.authentication.dtos.PagerDto;
import com.imbank.authentication.entities.AuditLog;
import com.imbank.authentication.services.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("audit-logs")
public class AuditLogController {

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(PagerDto pagerDto) {
        return ResponseEntity.ok().body(auditLogService.getAuditLogs(pagerDto));
    }

    @GetMapping("all")
    public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
        return ResponseEntity.ok().body(auditLogService.getAuditLogs());
    }

    @GetMapping("/apps/{appId}")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByApp(@PathVariable long appId, PagerDto pagerDto) {
        return ResponseEntity.ok().body(auditLogService.getAuditLogsByApp(appId, pagerDto));
    }

    @GetMapping("users/{userId}")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByUser(@PathVariable long userId, PagerDto pagerDto) {
        return ResponseEntity.ok().body(auditLogService.getAuditLogsByUser(userId, pagerDto));
    }

    @GetMapping("download/{startDate}/{endDate}")
    public ResponseEntity<Resource> downloadAuditLogs(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        String filename = String.format("LDAP Service Audit Logs From %s to %s.xlsx", simpleDateFormat.format(startDate), simpleDateFormat.format(endDate));
        InputStreamResource file = new InputStreamResource(auditLogService.downloadLogs(startDate, endDate));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

}
