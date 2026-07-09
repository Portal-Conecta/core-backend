package com.portal.conecta.hub.module.notification.infrastructure.messaging.dto;

import com.portal.conecta.hub.module.notification.domain.model.NotificationScopeType;
import jakarta.validation.constraints.NotNull;

public record NotificationScopePayload(
        @NotNull(message = "O tipo do escopo (type) é obrigatório.")
        NotificationScopeType type,

        String correlationId
) {}
