package com.portal.conecta.hub.module.notification.infrastructure.resolver;

import com.portal.conecta.hub.module.notification.domain.model.UserNotificationEntity;
import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class UserDirectResolver {

    private final UserNotificationRepository userNotificationRepository;

    public UserDirectResolver(UserNotificationRepository userNotificationRepository) {
        this.userNotificationRepository = userNotificationRepository;
    }

    public void insert(UUID notificationId, Set<UUID> userIds){
        if (userIds.isEmpty()) return;
        userNotificationRepository.insertUsersDirectly(notificationId, userIds);
    }
}
