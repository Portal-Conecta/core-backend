package com.portal.conecta.hub.module.notification.domain.port;

import com.portal.conecta.hub.module.notification.domain.model.UserNotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserNotificationRepository extends JpaRepository<UserNotificationEntity, UUID> {

    Optional<UserNotificationEntity> findByUserIdAndNotificationId(UUID userId, UUID notificationId);

    List<UserNotificationEntity> findAllByUserIdAndReadAtIsNull(UUID userId);

    boolean existsByNotificationIdAndUserId(UUID notificationId, UUID userId);

    @Query("""
        SELECT un FROM UserNotificationEntity un
        JOIN FETCH un.notification n
        WHERE un.user.id = :userId
          AND un.dismissedAt IS NULL
          AND (:unreadOnly = false OR un.readAt IS NULL)
        ORDER BY un.createdAt DESC
    """)
    Page<UserNotificationEntity> findVisibleByUserId(
            @Param("userId") UUID userId,
            @Param("unreadOnly") boolean unreadOnly,
            Pageable pageable
    );

    @Query("""
        SELECT COUNT(un) FROM UserNotificationEntity un
        WHERE un.user.id = :userId
          AND un.readAt IS NULL
          AND un.dismissedAt IS NULL
    """)
    long countUnreadByUserId(@Param("userId") UUID userId);

    @Modifying
    @Transactional(propagation = Propagation.MANDATORY)
    @Query(value = """
            INSERT INTO user_notifications (id, notification_id, user_id, created_at)
            SELECT gen_random_uuid(), :notificationId, u.id, NOW()
            FROM users u
            WHERE u.id IN (:userIds)
              AND u.active = true
              AND u.deleted_at IS NULL
              AND NOT EXISTS (
                  SELECT 1 FROM user_notifications un
                  WHERE un.notification_id = :notificationId AND un.user_id = u.id
              )
            """, nativeQuery = true)
    void insertUsersDirectly(
            @Param("notificationId") UUID notificationId,
            @Param("userIds") Set<UUID> userIds
    );

    @Modifying
    @Transactional(propagation = Propagation.MANDATORY)
    @Query(value = """
            INSERT INTO user_notifications (id, notification_id, user_id, created_at)
            SELECT gen_random_uuid(), :notificationId, uc.user_id, NOW()
            FROM user_classes uc
            JOIN users u  ON u.id  = uc.user_id
            JOIN classes c ON c.id = uc.class_id
            WHERE c.id IN (:classIds)
              AND c.deleted_at IS NULL
              AND c.active = true
              AND u.active = true
              AND u.deleted_at IS NULL
              AND u.type_user IN (:types)
              AND NOT EXISTS (
                  SELECT 1 FROM user_notifications un
                  WHERE un.notification_id = :notificationId AND un.user_id = uc.user_id
              )
            """, nativeQuery = true)
    void insertByClassScope(
            @Param("notificationId") UUID notificationId,
            @Param("classIds") List<UUID> classIds,
            @Param("types") Set<String> types
    );

    @Modifying
    @Transactional(propagation = Propagation.MANDATORY)
    @Query(value = """
            INSERT INTO user_notifications (id, notification_id, user_id, created_at)
            SELECT gen_random_uuid(), :notificationId, uc.user_id, NOW()
            FROM user_classes uc
            JOIN users u   ON u.id  = uc.user_id
            JOIN classes c ON c.id  = uc.class_id
            WHERE c.course_id IN (:courseIds)
              AND c.deleted_at IS NULL
              AND c.active = true
              AND u.active = true
              AND u.deleted_at IS NULL
              AND u.type_user IN (:types)
              AND NOT EXISTS (
                  SELECT 1 FROM user_notifications un
                  WHERE un.notification_id = :notificationId AND un.user_id = uc.user_id
              )
            """, nativeQuery = true)
    void insertByCourseScope(
            @Param("notificationId") UUID notificationId,
            @Param("courseIds") List<UUID> courseIds,
            @Param("types") Set<String> types
    );
}