package com.portal.conecta.hub.module.notification.aplication.use_case;

import com.portal.conecta.hub.module.notification.domain.exception.NotificationNotFoundException;
import com.portal.conecta.hub.module.notification.domain.model.UserNotificationEntity;
import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UpdateUserNotificationStatusUseCase {

    private final UserNotificationRepository repository;

    public UpdateUserNotificationStatusUseCase(UserNotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void markAsRead(UUID userId, UUID notificationId) {
        UserNotificationEntity userNotification = repository.findByUserIdAndNotificationId(userId, notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notificação não encontrada para o usuário informado."));

        userNotification.markAsRead();
        repository.save(userNotification);
    }

    @Transactional
    public void dismiss(UUID userId, UUID notificationId) {
        UserNotificationEntity userNotification = repository.findByUserIdAndNotificationId(userId, notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notificação não encontrada para o usuário informado."));

        userNotification.dismiss();
        repository.save(userNotification);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        List<UserNotificationEntity> visibleNotifications = repository.findAllVisibleAndUnreadByUserId(userId);
        if (visibleNotifications.isEmpty()) {
            return;
        }

        visibleNotifications.forEach(UserNotificationEntity::markAsRead);
        repository.saveAll(visibleNotifications);
    }
}
