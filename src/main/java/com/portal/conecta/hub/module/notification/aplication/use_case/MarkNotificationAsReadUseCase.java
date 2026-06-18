package com.portal.conecta.hub.module.notification.aplication.use_case;

import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import com.portal.conecta.hub.module.notification.domain.model.UserNotificationEntity;
import com.portal.conecta.hub.module.notification.domain.exception.NotificationNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class MarkNotificationAsReadUseCase {

    private final UserNotificationRepository repository;

    public MarkNotificationAsReadUseCase(UserNotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void execute(UUID userId, UUID notificationId) {
        UserNotificationEntity userNotification = repository.findByUserIdAndNotificationId(userId, notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notificação não encontrada para o usuário informado."));

        userNotification.markAsRead();
        repository.save(userNotification);
    }
}