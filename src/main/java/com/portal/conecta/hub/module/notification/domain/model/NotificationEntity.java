package com.portal.conecta.hub.module.notification.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.portal.conecta.hub.shared.config.JsonNodeConverter;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "notifications",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_notifications_message_id", columnNames = "message_id")
        }
)
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "message_id", nullable = false, length = 255)
    private String messageId;

    @Column(name = "correlation_id", length = 255)
    private String correlationId;

    @Column(name = "source", nullable = false, length = 150)
    private String source;

    @Column(name = "event_type", nullable = false, length = 150)
    private String eventType;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "metadata", columnDefinition = "TEXT")
    private JsonNode metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "notification", fetch = FetchType.LAZY)
    private Set<UserNotificationEntity> userNotifications = new LinkedHashSet<>();

    protected NotificationEntity() {
    }

    public NotificationEntity(
            String messageId,
            String correlationId,
            String source,
            String eventType,
            Instant occurredAt,
            String title,
            String body,
            JsonNode metadata
    ) {
        this.messageId = Objects.requireNonNull(messageId, "O messageId é obrigatório.");
        this.correlationId = correlationId;
        this.source = Objects.requireNonNull(source, "O source é obrigatório.");
        this.eventType = Objects.requireNonNull(eventType, "O eventType é obrigatório.");
        this.occurredAt = Objects.requireNonNull(occurredAt, "O occurredAt é obrigatório.");
        this.title = Objects.requireNonNull(title, "O title é obrigatório.");
        this.body = Objects.requireNonNull(body, "O body é obrigatório.");
        this.metadata = metadata;
    }

    public static NotificationEntity create(
            String messageId,
            String correlationId,
            String source,
            String eventType,
            Instant occurredAt,
            String title,
            String body,
            JsonNode metadata
    ) {
        return new NotificationEntity(messageId, correlationId, source, eventType, occurredAt, title, body, metadata);
    }

    @PrePersist
    private void prePersist() {
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getSource() {
        return source;
    }

    public String getEventType() {
        return eventType;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public JsonNode getMetadata() {
        return metadata;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Set<UserNotificationEntity> getUserNotifications() {
        return userNotifications;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof NotificationEntity that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return NotificationEntity.class.hashCode();
    }
}