package com.imbank.authentication.repositories;

import com.imbank.authentication.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    List<Permission> findAllByAppId(Long appId);


    @Modifying
    @Transactional
    void deleteAllByAppId(long id);
}
