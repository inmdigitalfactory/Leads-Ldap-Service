package com.imbank.authentication.config;

import com.imbank.authentication.utils.AuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * Implementation of {@link AuditorAware} based on Spring Security.
 */
@Configuration
@Slf4j
public class AuditorConfig implements AuditorAware<String> {

    @Value("${role.system:system}")
    private String systemAccount;

    @Override
    public Optional<String> getCurrentAuditor() {
        log.info("Checking logged in user");
        return Optional.of(AuthUtils.getLoggedInUser().orElse(systemAccount));
    }
}
