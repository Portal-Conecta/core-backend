package com.portal.conecta.hub.module.notification.application.use_case;

import com.portal.conecta.hub.module.notification.domain.model.UserNotificationEntity;
import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Lista as notificações visíveis do usuário autenticado.
 *
 * <p>Notificações descartadas não são retornadas. Quando {@code unreadOnly} é verdadeiro,
 * a consulta fica restrita aos vínculos de usuário ainda não lidos.</p>
 */
@Service
public class GetUserNotificationsUseCase {

    private final UserNotificationRepository repository;
    private final RequestContextProvider contextProvider;

    public GetUserNotificationsUseCase(
            UserNotificationRepository repository,
            RequestContextProvider contextProvider
    ) {
        this.repository = repository;
        this.contextProvider = contextProvider;
    }

    /**
     * Consulta notificações paginadas do usuário presente no contexto da requisição.
     *
     * @param unreadOnly indica se apenas notificações não lidas devem ser retornadas.
     * @param page página solicitada.
     * @param size tamanho da página.
     * @return página de notificações de usuário visíveis.
     */
    @Transactional(readOnly = true)
    public Page<UserNotificationEntity> execute(boolean unreadOnly, int page, int size) {
        UUID userId = contextProvider.getRequestContext().userId();
        return repository.findVisibleByUserId(userId, unreadOnly, PageRequest.of(page, size));
    }
}
