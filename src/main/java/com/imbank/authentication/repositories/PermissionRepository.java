package com.imbank.authentication.repositories;

import com.imbank.authentication.entities.AllowedApp;
import com.imbank.authentication.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    List<Permission> findAllByAppId(Long appId);


    @Modifying
    @Transactional
    void deleteAllByAppId(long id);


    Optional<Permission> findFirstByAppAndCodeIgnoreCase(AllowedApp app, String code);
}
