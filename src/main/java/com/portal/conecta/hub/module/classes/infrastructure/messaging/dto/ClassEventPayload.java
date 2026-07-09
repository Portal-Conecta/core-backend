package com.portal.conecta.hub.module.classes.infrastructure.messaging.dto;

import java.time.Instant;

public record ClassEventPayload(
        String eventId,
        String correlationId,
        String source,
        String eventType,
        Instant occurredAt,
        String entityType,
        String entityId,
        String name
) {
}
