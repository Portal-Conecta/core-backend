package com.portal.conecta.hub.module.notification.presentation.dto;

import java.util.List;
import java.util.UUID;

public record MarkAsReadNotificationsRequest(
        List<UUID> notificationIds
) {
}
