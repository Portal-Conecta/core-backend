package com.portal.conecta.hub.module.user.domain.port;

import java.time.Duration;

/**
 * Output port that provides the account activation token lifetime.
 */
public interface AccountActivationTokenTtlPort {

    /**
     * Returns how long a newly created activation token remains valid.
     *
     * @return activation token duration
     */
    Duration activationTokenTtl();
}
