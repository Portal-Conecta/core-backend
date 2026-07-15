package com.portal.conecta.hub.module.notification.domain.model;

import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class UserNotificationEntityTest {
    private final NotificationEntity notification = NotificationEntity.create("m", null, "core", "event", Instant.now(), "title", "body", null);
    private final UserEntity user = new UserEntity("User", "user@test.local", "hash", TypeUser.STUDENT);

    @Test void createRequiresBothAssociationsAndStartsUnreadAndVisible() {
        UserNotificationEntity entity = UserNotificationEntity.create(notification, user);
        assertSame(notification, entity.getNotification());
        assertSame(user, entity.getUser());
        assertFalse(entity.isRead());
        assertFalse(entity.isDismissed());
        assertThrows(NullPointerException.class, () -> UserNotificationEntity.create(null, user));
        assertThrows(NullPointerException.class, () -> UserNotificationEntity.create(notification, null));
    }

    @Test void readAndDismissAreIdempotent() {
        UserNotificationEntity entity = UserNotificationEntity.create(notification, user);
        entity.markAsRead();
        Instant readAt = entity.getReadAt();
        entity.markAsRead();
        assertSame(readAt, entity.getReadAt());
        assertTrue(entity.isRead());

        entity.dismiss();
        Instant dismissedAt = entity.getDismissedAt();
        entity.dismiss();
        assertSame(dismissedAt, entity.getDismissedAt());
        assertTrue(entity.isDismissed());
    }

    @Test void equalityUsesPersistentIdentity() {
        UserNotificationEntity first = UserNotificationEntity.create(notification, user);
        UserNotificationEntity same = UserNotificationEntity.create(notification, user);
        UserNotificationEntity other = UserNotificationEntity.create(notification, user);
        UUID id = UUID.randomUUID();
        ReflectionTestUtils.setField(first, "id", id);
        ReflectionTestUtils.setField(same, "id", id);
        ReflectionTestUtils.setField(other, "id", UUID.randomUUID());
        assertEquals(first, first);
        assertEquals(first, same);
        assertNotEquals(first, other);
        assertNotEquals(first, null);
        assertNotEquals(first, notification);
        assertNotEquals(UserNotificationEntity.create(notification, user), same);
    }
}
