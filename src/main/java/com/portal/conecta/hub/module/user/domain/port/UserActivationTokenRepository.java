package com.portal.conecta.hub.module.user.domain.port;

import com.portal.conecta.hub.module.user.domain.model.UserActivationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for persisted account activation tokens.
 */
public interface UserActivationTokenRepository extends JpaRepository<UserActivationTokenEntity, UUID> {

    /**
     * Finds a token entity by the stored token hash.
     *
     * @param tokenHash hash generated from the raw activation token
     * @return matching token entity when present
     */
    Optional<UserActivationTokenEntity> findByTokenHash(String tokenHash);
}
