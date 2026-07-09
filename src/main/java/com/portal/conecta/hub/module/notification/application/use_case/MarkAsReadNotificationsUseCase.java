package com.portal.conecta.hub.module.notification.application.use_case;

import com.portal.conecta.hub.module.notification.application.command.MarkAsReadNotificationsCommand;
import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import org.springframework.stereotype.Service;

/**
 * Marca como lidas as notificações de usuário especificadas no comando.
 */
@Service
public class MarkAsReadNotificationsUseCase {

    private final UserNotificationRepository repository;

    public MarkAsReadNotificationsUseCase(UserNotificationRepository repository) {
        this.repository = repository;
    }

    /**
     * Marca como lidas as notificações de usuário especificadas no comando.
     * @param command comando contendo os IDs das notificações a serem marcadas como lidas.
     */
    public void execute(MarkAsReadNotificationsCommand command) {
        repository.markAsReadNotifications(command.notificationIds());
    }

}
