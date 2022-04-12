package com.imbank.authentication.repositories;

import com.imbank.authentication.entities.AllowedApp;
import com.imbank.authentication.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findFirstByNameAndApp(String adminRoleName, AllowedApp app);

    List<Role> findAllByAppId(Long appId);
}
