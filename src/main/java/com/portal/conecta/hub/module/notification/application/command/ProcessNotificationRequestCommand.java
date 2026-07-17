package com.portal.conecta.hub.module.notification.application.command;

import com.portal.conecta.hub.module.notification.domain.exception.InvalidNotificationPayloadException;
import com.portal.conecta.hub.module.notification.infrastructure.messaging.dto.filter.NotificationFilterType;
import com.portal.conecta.hub.module.notification.infrastructure.messaging.dto.scope.NotificationScopeType;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ProcessNotificationRequestCommand(
        String messageId,
        String correlationId,
        String source,
        String eventType,
        Instant occurredAt,
        String title,
        String body,
        List<CommandFilter> filters,
        List<CommandScope> scopes,
        Map<String, Object> metadata
) {
    public ProcessNotificationRequestCommand {
        requireNonBlank(messageId, "messageId");
        requireNonBlank(source, "source");
        requireNonBlank(eventType, "eventType");
        requireNonNull(occurredAt, "occurredAt");
        requireNonBlank(title, "title");
        requireNonBlank(body, "body");
        if (scopes == null || scopes.isEmpty()) {
            throw new InvalidNotificationPayloadException("scope não pode ser vazio.");
        }
    }

    public record CommandFilter(NotificationFilterType type, String value) {}

    public record CommandScope(NotificationScopeType type, String correlationId) {}

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new InvalidNotificationPayloadException("Campo obrigatório ausente: " + field);
        }
    }

    private static void requireNonNull(Object value, String field) {
        if (value == null) {
            throw new InvalidNotificationPayloadException("Campo obrigatório ausente: " + field);
        }
    }
}
