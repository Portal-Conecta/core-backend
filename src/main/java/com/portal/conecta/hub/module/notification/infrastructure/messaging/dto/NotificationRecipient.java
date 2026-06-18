package com.portal.conecta.hub.module.notification.infrastructure.messaging.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record NotificationRecipient(
        @NotNull(message = "O escopo do destinatário é obrigatório.")
        @Valid
        NotificationRecipientScope scope,

        @Valid
        NotificationRecipientFilters filters
) {
}