package com.portal.conecta.hub.module.auth.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidade que representa um refresh token persistido.
 *
 * <p>O campo {@code createdAt} é preenchido automaticamente no momento
 * da persistência via {@code @PrePersist}.
 *
 * <p>Cada token é de uso único: após ser utilizado no fluxo de refresh,
 * é deletado e um novo par de tokens é gerado (rotation strategy).
 */
@Getter
@Setter
@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "token", nullable = false, unique = true, length = 512)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected RefreshTokenEntity() {}

    public RefreshTokenEntity(String token, Instant expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    private void prePersist() {
        this.createdAt = Instant.now();
    }

}