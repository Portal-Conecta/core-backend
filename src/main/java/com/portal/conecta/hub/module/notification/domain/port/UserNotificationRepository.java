package com.portal.conecta.hub.module.notification.domain.port;

import com.portal.conecta.hub.module.notification.domain.model.UserNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotificationEntity, UUID>{

    Optional<UserNotificationEntity> findByUserIdAndNotificationId(UUID userId, UUID notificationId);

    @Query("SELECT un FROM UserNotificationEntity un WHERE un.user.id = :userId AND un.dismissedAt IS NULL AND un.readAt IS NULL")
    List<UserNotificationEntity> findAllVisibleAndUnreadByUserId(@Param("userId") UUID userId);
}
