package com.portal.conecta.hub.module.user.infrastructure.activation;

import com.portal.conecta.hub.module.user.domain.port.AccountActivationTokenTtlPort;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Provides the default account activation token lifetime.
 */
@Component
public class FixedAccountActivationTokenTtlAdapter implements AccountActivationTokenTtlPort {

    /**
     * Returns the current fixed activation token lifetime.
     *
     * @return 24-hour token lifetime
     */
    @Override
    public Duration activationTokenTtl() {
        return Duration.ofHours(24);
    }
}
