package com.portal.conecta.hub.module.notification.domain.model;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import org.junit.jupiter.api.function.Executable;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NotificationEntityTest {
    @Test void createPreservesPayloadAllowsNullCorrelationAndStartsWithoutRecipients() {
        Instant occurredAt = Instant.now();
        JsonNode metadata = new ObjectMapper().createObjectNode().put("key", "value");
        NotificationEntity entity = NotificationEntity.create("message", null, "source", "event", occurredAt, "title", "body", metadata);
        assertAll(() -> assertEquals("message", entity.getMessageId()), () -> assertNull(entity.getCorrelationId()),
                () -> assertEquals("source", entity.getSource()), () -> assertEquals("event", entity.getEventType()),
                () -> assertSame(occurredAt, entity.getOccurredAt()), () -> assertEquals("title", entity.getTitle()),
                () -> assertEquals("body", entity.getBody()), () -> assertSame(metadata, entity.getMetadata()),
                () -> assertTrue(entity.getUserNotifications().isEmpty()));
    }

    @Test void rejectsEveryMissingRequiredField() {
        Instant now = Instant.now();
        Executable[] invalidFactories = {
                () -> NotificationEntity.create(null, null, "s", "e", now, "t", "b", null),
                () -> NotificationEntity.create("m", null, null, "e", now, "t", "b", null),
                () -> NotificationEntity.create("m", null, "s", null, now, "t", "b", null),
                () -> NotificationEntity.create("m", null, "s", "e", null, "t", "b", null),
                () -> NotificationEntity.create("m", null, "s", "e", now, null, "b", null),
                () -> NotificationEntity.create("m", null, "s", "e", now, "t", null, null)
        };
        for (Executable factory : invalidFactories) assertThrows(NullPointerException.class, factory);
    }

    @Test void equalityUsesPersistentIdentity() {
        NotificationEntity first = NotificationEntity.create("m1", null, "s", "e", Instant.now(), "t", "b", null);
        NotificationEntity same = NotificationEntity.create("m2", null, "s", "e", Instant.now(), "t", "b", null);
        NotificationEntity other = NotificationEntity.create("m3", null, "s", "e", Instant.now(), "t", "b", null);
        UUID id = UUID.randomUUID();
        ReflectionTestUtils.setField(first, "id", id);
        ReflectionTestUtils.setField(same, "id", id);
        ReflectionTestUtils.setField(other, "id", UUID.randomUUID());
        assertEquals(first, first);
        assertEquals(first, same);
        assertNotEquals(first, other);
        assertNotEquals(first, null);
        assertNotEquals(first, "notification");
        assertNotEquals(NotificationEntity.create("new", null, "s", "e", Instant.now(), "t", "b", null), same);
    }
}
