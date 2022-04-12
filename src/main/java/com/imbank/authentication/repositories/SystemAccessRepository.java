package com.imbank.authentication.repositories;

import com.imbank.authentication.entities.AllowedApp;
import com.imbank.authentication.entities.SystemAccess;
import com.imbank.authentication.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemAccessRepository extends JpaRepository<SystemAccess, Long> {

    Optional<SystemAccess> findFirstByUserAndApp(User user, AllowedApp app);
}
