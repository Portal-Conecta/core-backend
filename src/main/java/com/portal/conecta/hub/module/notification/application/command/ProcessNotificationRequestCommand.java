package com.portal.conecta.hub.module.notification.application.command;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record ProcessNotificationRequestCommand(
        String messageId,
        String correlationId,
        String source,
        String eventType,
        OffsetDateTime occurredAt,
        String title,
        String body,
        List<Recipient> recipients,
        Map<String, Object> metadata
) {
    public record Recipient(
            Scope scope,
            Filters filters
    ) {}

    public record Scope(
            String type,
            String id
    ) {}

    public record Filters(
            List<String> userTypes,
            List<String> roles
    ) {}
}