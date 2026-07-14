package com.portal.conecta.hub.module.notification.infrastructure.resolver;

import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Resolve distribuição global para usuários ativos do Hub Core.
 */
@Component
public class GlobalScopeResolver {

    private final UserNotificationRepository userNotificationRepository;

    public GlobalScopeResolver(UserNotificationRepository userNotificationRepository) {
        this.userNotificationRepository = userNotificationRepository;
    }

    /**
     * Cria vínculos de notificação para usuários ativos e não removidos logicamente.
     *
     * @param notificationId identificador da notificação global.
     * @param types tipos de usuário permitidos pelo filtro ROLE; vazio considera todos.
     */
    public void insert(UUID notificationId, EnumSet<TypeUser> types) {
        if (types.isEmpty()) {
            userNotificationRepository.insertByGlobalScope(notificationId);
            return;
        }

        Set<String> typeNames = types.stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        userNotificationRepository.insertByGlobalScopeFilteredByRole(notificationId, typeNames);
    }
}
