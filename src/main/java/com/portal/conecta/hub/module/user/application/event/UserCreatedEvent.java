package com.portal.conecta.hub.module.user.application.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published after a user and its activation token are created.
 *
 * <p>Listeners should handle this event after the database transaction commits,
 * so activation e-mails are only sent for persisted users.</p>
 *
 * @param userId identifier of the created user
 * @param name display name used in the activation e-mail
 * @param email destination address
 * @param rawActivationToken raw token that must be delivered only to the user
 * @param expiresAt expiration instant for the activation token
 */
public record UserCreatedEvent(
        UUID userId,
        String name,
        String email,
        String rawActivationToken,
        Instant expiresAt
) {
}
