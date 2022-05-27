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
import com.imbank.authentication.utils.Utils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy HH:mm");

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
        if(!ObjectUtils.isEmpty(pager.getAction())) {
            return auditRepository.findAllByActionIn(Utils.getActions(pager.getAction()), pageRequest);
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
        if(!ObjectUtils.isEmpty(pager.getAction())) {
            return auditRepository.findAllByAppIdAndActionIn(appId, Utils.getActions(pager.getAction()), pageRequest);
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
        if(!ObjectUtils.isEmpty(pager.getAction())) {
            return auditRepository.findAllByUserIdAndActionIn(userId, Utils.getActions(pager.getAction()), pageRequest);
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

    @Override
    @Transactional
    public ByteArrayInputStream downloadLogs(Date startDate, Date endDate) {
        String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        String[] headers = { "ID", "Name", "Email", "Action", "Description", "Date" };
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            Sheet sheet = workbook.createSheet("Audit Logs");
            // Header
            Row headerRow = sheet.createRow(0);
            CellStyle cellStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            cellStyle.setFont(font);
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(cellStyle);
            }

            CellStyle createStyle = workbook.createCellStyle();
            Font createFont = workbook.createFont();
            createFont.setBold(true);
            createFont.setColor(IndexedColors.GREEN.getIndex());
//            createStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            createStyle.setFont(font);

            CellStyle updateStyle = workbook.createCellStyle();
            Font updateFont = workbook.createFont();
            updateFont.setBold(true);
            updateFont.setColor(IndexedColors.ORANGE.getIndex());
//            updateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            updateStyle.setFont(updateFont);

            CellStyle deleteStyle = workbook.createCellStyle();
            Font deleteFont = workbook.createFont();
            deleteFont.setBold(true);
            deleteFont.setColor(IndexedColors.RED.getIndex());
//            deleteStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            deleteStyle.setFont(deleteFont);

            Map<String, User> users = userRepository.findAll().stream().collect(Collectors.toMap(User::getUsername, Function.identity(), (a,b)-> a));
            AtomicInteger rowIdx = new AtomicInteger(1);
            AtomicInteger counter = new AtomicInteger(1);
            LocalDateTime endOfDay = LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault()).with(LocalTime.MAX);
            auditRepository.findAllByCreatedOnBetween(startDate, Date.from(endOfDay.toInstant(ZoneOffset.UTC)))
                    .forEach(auditLog -> {
                        Row row = sheet.createRow(rowIdx.getAndIncrement());
                        row.createCell(0).setCellValue(counter.getAndIncrement());
                        row.createCell(1).setCellValue(users.containsKey(auditLog.getCreatedBy()) ? users.get(auditLog.getCreatedBy()).getName() : auditLog.getCreatedBy());
                        row.createCell(2).setCellValue(users.containsKey(auditLog.getCreatedBy()) ? users.get(auditLog.getCreatedBy()).getEmail() : "");

                        String crudAction = Utils.mapCrud(auditLog.getAction());
                        Cell cell = row.createCell(3);
                        switch (crudAction) {
                            case "CREATE":
                                cell.setCellStyle(createStyle);
                                break;
                            case "UPDATE":
                                cell.setCellStyle(updateStyle);
                                break;
                            case "DELETE":
                                cell.setCellStyle(deleteStyle);
                                break;
                        }
                        cell.setCellValue(crudAction);

                        row.createCell(4).setCellValue(Utils.mapCrudDescription(auditLog, objectMapper));
                        row.createCell(5).setCellValue(dateFormatter.format(auditLog.getCreatedOn()));
                    });
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }
}
