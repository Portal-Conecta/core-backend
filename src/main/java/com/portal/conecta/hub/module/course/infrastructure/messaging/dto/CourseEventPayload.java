package com.portal.conecta.hub.module.course.infrastructure.messaging.dto;

import java.time.Instant;

public record CourseEventPayload(
        String eventId,
        String correlationId,
        String source,
        String eventType,
        Instant occurredAt,
        String entityType,
        String entityId,
        String name,
        String code
) {
}
