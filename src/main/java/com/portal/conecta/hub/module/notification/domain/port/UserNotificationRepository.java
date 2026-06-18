package com.portal.conecta.hub.module.notification.domain.port;

import com.portal.conecta.hub.module.notification.domain.model.UserNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserNotificationRepository extends JpaRepository<UserNotificationEntity, UUID> {

    Optional<UserNotificationEntity> findByUserIdAndNotificationId(UUID userId, UUID notificationId);

    List<UserNotificationEntity> findAllByUserIdAndReadAtIsNull(UUID userId);

}