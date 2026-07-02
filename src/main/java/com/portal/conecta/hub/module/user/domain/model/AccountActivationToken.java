package com.portal.conecta.hub.module.user.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain view of an account activation token.
 *
 * <p>The raw token is intentionally not exposed by this model. Persistence
 * adapters should store and query tokens through a non-reversible hash.</p>
 *
 * @param userId user associated with the token
 * @param expiresAt instant when the token stops being valid
 * @param usedAt instant when the token was consumed, or {@code null} when unused
 */
public record AccountActivationToken(
        UUID userId,
        Instant expiresAt,
        Instant usedAt
) {

    /**
     * Checks whether the token is expired for the supplied instant.
     *
     * @param now instant used as the validation reference
     * @return {@code true} when the token is no longer valid
     */
    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    /**
     * Checks whether the token has already been consumed.
     *
     * @return {@code true} when the token has a usage timestamp
     */
    public boolean isUsed() {
        return usedAt != null;
    }
}
