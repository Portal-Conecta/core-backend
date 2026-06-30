package com.portal.conecta.hub.module.user.domain.port;

import com.portal.conecta.hub.module.user.domain.model.UserEntity;

import java.time.Instant;

/**
 * Output port responsible for requesting the delivery of account activation instructions.
 */
public interface AccountActivationNotificationPort {

    /**
     * Requests activation notification for a newly created user.
     *
     * @param user user that must activate the account
     * @param rawToken raw activation token to be included in the activation link
     * @param expiresAt expiration instant for the token
     */
    void requestActivation(UserEntity user, String rawToken, Instant expiresAt);
}
