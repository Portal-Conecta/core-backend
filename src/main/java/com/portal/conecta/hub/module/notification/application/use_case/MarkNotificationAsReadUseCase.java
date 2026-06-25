package com.portal.conecta.hub.module.notification.application.use_case;

import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import com.portal.conecta.hub.module.notification.domain.model.UserNotificationEntity;
import com.portal.conecta.hub.module.notification.domain.exception.NotificationNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@Slf4j
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

        log.info("Notificação marcada como lida. notificationId={}, userId={}", notificationId, userId);
    }
}