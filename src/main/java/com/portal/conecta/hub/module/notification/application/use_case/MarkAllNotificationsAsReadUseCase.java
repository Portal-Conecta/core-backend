package com.portal.conecta.hub.module.notification.application.use_case;

import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import com.portal.conecta.hub.module.notification.domain.model.UserNotificationEntity;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
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

    private final RequestContextProvider contextProvider;

    public MarkAllNotificationsAsReadUseCase(UserNotificationRepository repository, RequestContextProvider contextProvider) {
        this.repository = repository;
        this.contextProvider = contextProvider;
    }

    /**
     * Registra leitura para todas as notificações ainda não lidas do usuário.
     */
    @Transactional
    public void execute() {
        var context = contextProvider.getRequestContext();

        repository.readAllNotificationByUserId(context.userId());

        log.info("Notificações marcadas como lidas em lote. userId={}", context.userId());
    }
}
