package com.imbank.authentication.repositories;

import com.imbank.authentication.entities.AllowedApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AllowedAppRepository extends JpaRepository<AllowedApp, Long> {

    AllowedApp findFirstByAccessTokenAndEnabled(String accessToken, boolean enabled);
}
