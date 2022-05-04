package com.imbank.authentication.repositories;

import com.imbank.authentication.entities.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findAllByAppId(long appId, Pageable pageRequest);

    Page<AuditLog> findAllByUserId(long userId, Pageable pageRequest);
}
