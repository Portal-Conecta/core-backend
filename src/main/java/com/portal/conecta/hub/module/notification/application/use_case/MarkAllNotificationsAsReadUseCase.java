package com.portal.conecta.hub.module.notification.application.use_case;

import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import com.portal.conecta.hub.module.notification.domain.model.UserNotificationEntity;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

/**
 * Marca em lote todas as notificações não lidas de um usuário.
 *
 * <p>A operação altera apenas registros visíveis em {@link UserNotificationEntity}
 * associados ao usuário, mantendo intacta a notificação global.</p>
 */
@Service
@Slf4j
public class MarkAllNotificationsAsReadUseCase {

    private final UserNotificationRepository repository;

    public MarkAllNotificationsAsReadUseCase(UserNotificationRepository repository) {
        this.repository = repository;
    }

    /**
     * Registra leitura para todas as notificações ainda não lidas do usuário.
     *
     * @param userId identificador do usuário destinatário.
     */
    @Transactional
    public void execute(UUID userId) {
        List<UserNotificationEntity> unreadNotifications = repository.findAllByUserIdAndReadAtIsNull(userId);
        unreadNotifications.forEach(UserNotificationEntity::markAsRead);
        repository.saveAll(unreadNotifications);

        log.info("Notificações marcadas como lidas em lote. userId={}, updatedCount={}", userId, unreadNotifications.size());
    }
}
