package com.portal.conecta.hub.module.user.infrastructure.activation;

import com.portal.conecta.hub.module.user.application.event.UserCreatedEvent;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.AccountActivationNotificationPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Publishes account activation requests as Spring application events.
 */
@Component
public class ApplicationEventAccountActivationNotificationAdapter implements AccountActivationNotificationPort {

    private final ApplicationEventPublisher eventPublisher;

    public ApplicationEventAccountActivationNotificationAdapter(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Publishes the user creation event with the raw token required by the e-mail listener.
     *
     * @param user user that must activate the account
     * @param rawToken raw activation token to be included in the activation link
     * @param expiresAt expiration instant for the token
     */
    @Override
    public void requestActivation(UserEntity user, String rawToken, Instant expiresAt) {
        eventPublisher.publishEvent(new UserCreatedEvent(
                user.getId(),
                user.getName(),
                user.getEmail(),
                rawToken,
                expiresAt
        ));
    }
}
