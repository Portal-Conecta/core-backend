package com.portal.conecta.hub.module.notification.infrastructure.messaging.dto;

import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record NotificationMessagePayload(
        @NotBlank(message = "O ID da mensagem é obrigatório.")
        String messageId,

        String correlationId,

        @NotBlank(message = "A origem (source) é obrigatória.")
        String source,

        @NotBlank(message = "O tipo de evento (eventType) é obrigatório.")
        String eventType,

        @NotNull(message = "A data de ocorrência (occurredAt) é obrigatória.")
        Instant occurredAt,

        @NotBlank(message = "O título é obrigatório.")
        String title,

        @NotBlank(message = "O corpo da mensagem (body) é obrigatório.")
        String body,

        @Valid
        List<NotificationFilterPayload> filters,

        @NotEmpty(message = "Pelo menos um escopo deve ser informado.")
        @Valid
        List<NotificationScopePayload> scope,

        Map<String, Object> metadata
) {
    public ProcessNotificationRequestCommand toCommand() {
        var commandFilters = filters != null ? filters.stream()
                .map(f -> new ProcessNotificationRequestCommand.CommandFilter(f.type(), f.value()))
                .toList() : List.<ProcessNotificationRequestCommand.CommandFilter>of();

        var commandScopes = scope != null ? scope.stream()
                .map(s -> new ProcessNotificationRequestCommand.CommandScope(s.type(), s.correlationId()))
                .toList() : List.<ProcessNotificationRequestCommand.CommandScope>of();

        return new ProcessNotificationRequestCommand(
                messageId,
                correlationId,
                source,
                eventType,
                occurredAt,
                title,
                body,
                commandFilters,
                commandScopes,
                metadata != null ? metadata : Map.of()
        );
    }
}