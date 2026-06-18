package com.portal.conecta.hub.module.notification.infrastructure.messaging.dto;

import java.util.List;

public record NotificationRecipientFilters(
        List<String> userTypes,
        List<String> roles
) {
}