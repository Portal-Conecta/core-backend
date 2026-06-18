package com.portal.conecta.hub.module.notification.aplication.use_case;

import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import com.portal.conecta.hub.module.notification.domain.model.UserNotificationEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class MarkAllNotificationsAsReadUseCase {

    private final UserNotificationRepository repository;

    public MarkAllNotificationsAsReadUseCase(UserNotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void execute(UUID userId) {
        List<UserNotificationEntity> unreadNotifications = repository.findAllByUserIdAndReadAtIsNull(userId);
        unreadNotifications.forEach(UserNotificationEntity::markAsRead);
        repository.saveAll(unreadNotifications);
    }
}