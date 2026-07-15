package com.portal.conecta.hub.module.notification.application.use_case;

import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import com.portal.conecta.hub.module.notification.domain.model.UserNotificationEntity;
import com.portal.conecta.hub.module.notification.domain.exception.NotificationNotFoundException;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

/**
 * Descarta uma notificação previamente distribuída para um usuário.
 *
 * <p>O descarte é individual: altera apenas o vínculo em {@link UserNotificationEntity}
 * e não remove a notificação global nem os vínculos de outros usuários.</p>
 */
@Service
@Slf4j
public class DismissNotificationUseCase {

    private final UserNotificationRepository repository;

    private final RequestContextProvider requestContextProvider;

    public DismissNotificationUseCase(UserNotificationRepository repository, RequestContextProvider requestContextProvider) {
        this.repository = repository;
        this.requestContextProvider = requestContextProvider;
    }

    /**
     * Marca a notificação do usuário como descartada.
     *
     * @param notificationId identificador da notificação global.
     * @throws NotificationNotFoundException quando a notificação não foi distribuída para o usuário.
     */
    @Transactional
    public void execute(UUID notificationId) {
        var context = requestContextProvider.getRequestContext();

        UserNotificationEntity userNotification = repository.findByUserIdAndNotificationId(context.userId(), notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notificação não encontrada para o usuário informado."));

        userNotification.dismiss();
        repository.save(userNotification);

        log.info("Notificação descartada pelo usuário. notificationId={}, userId={}", notificationId, context.userId());
    }
}
