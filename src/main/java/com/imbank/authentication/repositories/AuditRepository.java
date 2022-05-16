package com.imbank.authentication.repositories;

import com.imbank.authentication.entities.AuditLog;
import com.imbank.authentication.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@Repository
public interface AuditRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findAllByAppId(long appId, Pageable pageRequest);

    Page<AuditLog> findAllByUserId(long userId, Pageable pageRequest);

    List<AuditLog> findAllByOrderByIdDesc();

    Page<AuditLog> findAllByActionIn(List<AuditAction> actions, Pageable pageRequest);

    Page<AuditLog> findAllByAppIdAndActionIn(long appId, List<AuditAction> actions, Pageable pageRequest);

    Page<AuditLog> findAllByUserIdAndActionIn(long userId, List<AuditAction> actions, Pageable pageRequest);

    Stream<AuditLog> findAllByCreatedOnBetween(Date startDate, Date endDate);
}
