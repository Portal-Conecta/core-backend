package com.portal.conecta.hub.module.notification.domain.port;

import com.portal.conecta.hub.module.notification.domain.model.UserNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserNotificationRepository extends JpaRepository<UserNotificationEntity, UUID> {

    Optional<UserNotificationEntity> findByUserIdAndNotificationId(UUID userId, UUID notificationId);

    List<UserNotificationEntity> findAllByUserIdAndReadAtIsNull(UUID userId);

    boolean existsByNotificationIdAndUserId(UUID notificationId, UUID userId);

    @Modifying
    @Query(value = """
        INSERT INTO user_notifications (id, notification_id, user_id, created_at)
        SELECT gen_random_uuid(), :notificationId, u.id, now()
        FROM users u
        WHERE u.id IN (:userIds)
          AND u.active = true
          AND u.deleted_at IS NULL
        ON CONFLICT (notification_id, user_id) DO NOTHING
        """, nativeQuery = true)
    void insertForUsers(
            @Param("notificationId") UUID notificationId,
            @Param("userIds") List<UUID> userIds
    );
}
