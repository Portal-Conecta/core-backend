package com.portal.conecta.hub.module.notification.presentation.dto;

import com.portal.conecta.hub.module.notification.domain.model.UserNotificationEntity;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID notificationId,
        String title,
        String body,
        String source,
        String eventType,
        Instant occurredAt,
        Instant readAt,
        Instant createdAt,
        JsonNode metadata
) {
    public static NotificationResponse from(UserNotificationEntity un) {
        return new NotificationResponse(
                un.getId(),
                un.getNotification().getId(),
                un.getNotification().getTitle(),
                un.getNotification().getBody(),
                un.getNotification().getSource(),
                un.getNotification().getEventType(),
                un.getNotification().getOccurredAt(),
                un.getReadAt(),
                un.getCreatedAt(),
                un.getNotification().getMetadata()
        );
    }
}