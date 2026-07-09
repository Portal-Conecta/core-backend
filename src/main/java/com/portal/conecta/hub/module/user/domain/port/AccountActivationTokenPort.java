package com.portal.conecta.hub.module.user.domain.port;

import com.portal.conecta.hub.module.user.domain.model.AccountActivationToken;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;

import java.time.Instant;
import java.util.Optional;

/**
 * Output port for creating and consuming account activation tokens.
 */
public interface AccountActivationTokenPort {

    /**
     * Creates a new activation token for the given user.
     *
     * @param user user associated with the token
     * @param expiresAt expiration instant for the token
     * @return raw token that can be sent to the user exactly once
     */
    String createToken(UserEntity user, Instant expiresAt);

    /**
     * Finds a token from its raw value.
     *
     * @param rawToken raw token received from the activation request
     * @return token metadata when the token exists
     */
    Optional<AccountActivationToken> findByRawToken(String rawToken);

    /**
     * Marks a token as used.
     *
     * @param rawToken raw token received from the activation request
     */
    void markAsUsed(String rawToken);
}
