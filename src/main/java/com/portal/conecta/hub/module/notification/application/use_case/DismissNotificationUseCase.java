package com.portal.conecta.hub.module.notification.application.use_case;

import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import com.portal.conecta.hub.module.notification.domain.model.UserNotificationEntity;
import com.portal.conecta.hub.module.notification.domain.exception.NotificationNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class DismissNotificationUseCase {

    private final UserNotificationRepository repository;

    public DismissNotificationUseCase(UserNotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void execute(UUID userId, UUID notificationId) {
        UserNotificationEntity userNotification = repository.findByUserIdAndNotificationId(userId, notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notificação não encontrada para o usuário informado."));

        userNotification.dismiss();
        repository.save(userNotification);
    }
}