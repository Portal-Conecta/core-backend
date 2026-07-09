package com.portal.conecta.hub.module.notification.application.command;

import java.util.List;
import java.util.UUID;

public record MarkAsReadNotificationsCommand(
        List<UUID> notificationIds
) {
}
