package com.portal.conecta.hub.module.notification.domain.model;

import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Vínculo entre uma notificação global e um usuário destinatário.
 *
 * <p>Esta entidade concentra o ciclo de vida individual da notificação para o usuário:
 * criação do vínculo, leitura e descarte. O descarte remove a notificação das consultas
 * visíveis do usuário, sem apagar a notificação global.</p>
 */
@Entity
@Table(
        name = "user_notifications",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_notifications_notification_id_user_id",
                        columnNames = {"notification_id", "user_id"}
                )
        }
)
public class UserNotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false, updatable = false)
    private NotificationEntity notification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private UserEntity user;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "dismissed_at")
    private Instant dismissedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected UserNotificationEntity() {
    }

    public UserNotificationEntity(NotificationEntity notification, UserEntity user) {
        this.notification = Objects.requireNonNull(notification, "A notificação é obrigatória.");
        this.user = Objects.requireNonNull(user, "O usuário é obrigatório.");
    }

    public static UserNotificationEntity create(NotificationEntity notification, UserEntity user) {
        return new UserNotificationEntity(notification, user);
    }

    @PrePersist
    private void prePersist() {
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public NotificationEntity getNotification() {
        return notification;
    }

    public UserEntity getUser() {
        return user;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public Instant getDismissedAt() {
        return dismissedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isRead() {
        return readAt != null;
    }

    public boolean isDismissed() {
        return dismissedAt != null;
    }

    public void markAsRead() {
        if (this.readAt == null) {
            this.readAt = Instant.now();
        }
    }

    public void dismiss() {
        if (this.dismissedAt == null) {
            this.dismissedAt = Instant.now();
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof UserNotificationEntity that)) {
            return false;
        }

        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return UserNotificationEntity.class.hashCode();
    }

}
