package com.imbank.authentication.repositories;

import com.imbank.authentication.entities.SystemAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemAccessRepository extends JpaRepository<SystemAccess, Long> {

}
