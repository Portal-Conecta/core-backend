package com.portal.conecta.hub.module.notification.infrastructure.messaging.dto.filter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationFilterPayload(
        @NotNull(message = "O tipo do filtro (type) é obrigatório.")
        NotificationFilterType type,

        @NotBlank(message = "O valor do filtro (value) é obrigatório.")
        String value
) {}
