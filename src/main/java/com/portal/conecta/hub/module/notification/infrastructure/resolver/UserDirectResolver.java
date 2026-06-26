package com.portal.conecta.hub.module.notification.infrastructure.resolver;

import com.portal.conecta.hub.module.notification.domain.model.UserNotificationEntity;
import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

/**
 * Resolve distribuição direta para usuários informados explicitamente no escopo USER.
 */
@Component
public class UserDirectResolver {

    private final UserNotificationRepository userNotificationRepository;

    public UserDirectResolver(UserNotificationRepository userNotificationRepository) {
        this.userNotificationRepository = userNotificationRepository;
    }

    /**
     * Cria vínculos de notificação para usuários ativos e não removidos logicamente.
     *
     * @param notificationId identificador da notificação global.
     * @param userIds usuários destinatários informados diretamente.
     */
    public void insert(UUID notificationId, Set<UUID> userIds){
        if (userIds.isEmpty()) return;
        userNotificationRepository.insertUsersDirectly(notificationId, userIds);
    }
}
