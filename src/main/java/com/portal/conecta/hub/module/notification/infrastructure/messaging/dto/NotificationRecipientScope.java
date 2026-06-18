package com.portal.conecta.hub.module.notification.infrastructure.messaging.dto;

import jakarta.validation.constraints.NotBlank;

public record NotificationRecipientScope(
        @NotBlank(message = "O tipo do escopo (type) é obrigatório.")
        String type,

        @NotBlank(message = "O ID do escopo é obrigatório.")
        String id
) {
}