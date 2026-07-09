package com.portal.conecta.hub.module.notification.application.command;

import com.portal.conecta.hub.module.notification.presentation.dto.MarkAsReadNotificationsRequest;

import java.util.List;
import java.util.UUID;

public record MarkAsReadNotificationsCommand(
        List<UUID> notificationIds
) {
    public static MarkAsReadNotificationsCommand from(MarkAsReadNotificationsRequest request) {
        return new MarkAsReadNotificationsCommand(request.notificationIds());
    }
}
