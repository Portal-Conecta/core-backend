package com.portal.conecta.hub.module.notification.application.use_case;

import com.portal.conecta.hub.module.notification.application.command.MarkAsReadNotificationsCommand;
import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Marca como lidas as notificações de usuário especificadas no comando.
 */
@Service
public class MarkAsReadNotificationsUseCase {

    private final UserNotificationRepository repository;

    private final RequestContextProvider contextProvider;

    public MarkAsReadNotificationsUseCase(UserNotificationRepository repository, RequestContextProvider contextProvider) {
        this.repository = repository;
        this.contextProvider = contextProvider;
    }

    /**
     * Marca como lidas as notificações de usuário especificadas no comando.
     * @param command comando contendo os IDs das notificações a serem marcadas como lidas.
     */
    @Transactional
    public void execute(MarkAsReadNotificationsCommand command) {
        var context = contextProvider.getRequestContext();

        repository.markAsReadNotifications(command.notificationIds(), context.userId());
    }

}
