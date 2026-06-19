package com.portal.conecta.hub.module.notification.domain.port;

import com.portal.conecta.hub.module.notification.domain.model.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    Optional<NotificationEntity> findByMessageId(String messageId);
}
