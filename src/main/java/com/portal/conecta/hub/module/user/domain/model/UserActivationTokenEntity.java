package com.portal.conecta.hub.module.user.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Persistence entity for account activation tokens.
 *
 * <p>The entity stores only the token hash, never the raw token delivered to
 * the user.</p>
 */
@Entity
@Table(name = "user_activation_tokens")
public class UserActivationTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected UserActivationTokenEntity() {
    }

    /**
     * Creates a token entity linked to a user.
     *
     * @param user user that must activate the account
     * @param tokenHash non-reversible hash of the raw activation token
     * @param expiresAt expiration instant for the token
     */
    public UserActivationTokenEntity(UserEntity user, String tokenHash, Instant expiresAt) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    private void prePersist() {
        createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Marks this token as consumed at the current instant.
     */
    public void markAsUsed() {
        usedAt = Instant.now();
    }

    /**
     * Converts the persistence entity into the domain token view used by use cases.
     *
     * @return domain token without exposing the raw token value
     */
    public AccountActivationToken toDomainToken() {
        return new AccountActivationToken(user.getId(), expiresAt, usedAt);
    }
}
