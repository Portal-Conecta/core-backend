package com.portal.conecta.hub.module.notification.application.command;

import com.portal.conecta.hub.module.notification.domain.model.NotificationFilterType;
import com.portal.conecta.hub.module.notification.domain.model.NotificationScopeType;

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
    public record CommandFilter(
            NotificationFilterType type,
            String value) {}
    public record CommandScope(
            NotificationScopeType type,
            String correlationId) {}
}