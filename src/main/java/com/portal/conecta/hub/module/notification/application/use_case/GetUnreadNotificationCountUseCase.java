package com.portal.conecta.hub.module.notification.application.use_case;

import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Calcula a quantidade de notificações não lidas do usuário autenticado.
 *
 * <p>O contador considera apenas notificações de usuário não descartadas.</p>
 */
@Service
public class GetUnreadNotificationCountUseCase {

    private final UserNotificationRepository repository;
    private final RequestContextProvider contextProvider;

    public GetUnreadNotificationCountUseCase(
            UserNotificationRepository repository,
            RequestContextProvider contextProvider
    ) {
        this.repository = repository;
        this.contextProvider = contextProvider;
    }

    /**
     * Retorna o total de notificações não lidas para o usuário do contexto atual.
     *
     * @return quantidade de notificações não lidas e visíveis.
     */
    @Transactional(readOnly = true)
    public long execute() {
        UUID userId = contextProvider.getRequestContext().userId();
        return repository.countUnreadByUserId(userId);
    }
}
