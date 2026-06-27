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

/**
 * Marca como lida uma notificação já distribuída para o usuário autenticado.
 *
 * <p>A operação atua sobre {@link UserNotificationEntity}, não sobre a notificação global,
 * preservando o estado individual de leitura por usuário.</p>
 */
@Service
@Slf4j
public class MarkNotificationAsReadUseCase {


    private final UserNotificationRepository repository;

    public MarkNotificationAsReadUseCase(UserNotificationRepository repository) {
        this.repository = repository;
    }

    /**
     * Registra a leitura da notificação para o usuário informado.
     *
     * @param userId identificador do usuário destinatário.
     * @param notificationId identificador da notificação global.
     * @throws NotificationNotFoundException quando a notificação não foi distribuída para o usuário.
     */
    @Transactional
    public void execute(UUID userId, UUID notificationId) {
        UserNotificationEntity userNotification = repository.findByUserIdAndNotificationId(userId, notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notificação não encontrada para o usuário informado."));

        userNotification.markAsRead();
        repository.save(userNotification);

        log.info("Notificação marcada como lida. notificationId={}, userId={}", notificationId, userId);
    }
}
