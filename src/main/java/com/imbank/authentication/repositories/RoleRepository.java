package com.imbank.authentication.repositories;

import com.imbank.authentication.entities.AllowedApp;
import com.imbank.authentication.entities.Permission;
import com.imbank.authentication.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findFirstByAppAndNameIgnoreCase(AllowedApp app, String role);

    List<Role> findAllByAppId(Long appId);

    @Modifying
    @Transactional
    void deleteAllByAppId(long id);

    List<Role> findAllByPermissions(Permission permission);
}
