package com.portal.conecta.hub.module.notification.infrastructure.messaging.dto;

import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record NotificationRequestPayload(
        @NotBlank(message = "O ID da mensagem é obrigatório.")
        String messageId,

        @NotBlank(message = "O correlationId é obrigatório.")
        String correlationId,

        @NotBlank(message = "A origem (source) é obrigatória.")
        String source,

        @NotBlank(message = "O tipo de evento (eventType) é obrigatório.")
        String eventType,

        @NotNull(message = "A data de ocorrência (occurredAt) é obrigatória.")
        OffsetDateTime occurredAt,

        @NotBlank(message = "O título é obrigatório.")
        String title,

        @NotBlank(message = "O corpo da mensagem (body) é obrigatório.")
        String body,

        @NotEmpty(message = "A lista de destinatários não pode ser vazia.")
        List<@NotNull(message = "O destinatário não pode ser nulo.") @Valid NotificationRecipient> recipients,

        Map<String, Object> metadata
) {
    public ProcessNotificationRequestCommand toCommand() {
        var mappedRecipients = this.recipients.stream()
                .map(r -> new ProcessNotificationRequestCommand.Recipient(
                        r.scope() != null ? new ProcessNotificationRequestCommand.Scope(r.scope().type(), r.scope().id()) : null,
                        r.filters() != null ? new ProcessNotificationRequestCommand.Filters(r.filters().userTypes(), r.filters().roles()) : null
                )).toList();

        return new ProcessNotificationRequestCommand(
                this.messageId,
                this.correlationId,
                this.source,
                this.eventType,
                this.occurredAt,
                this.title,
                this.body,
                mappedRecipients,
                this.metadata
        );
    }
}