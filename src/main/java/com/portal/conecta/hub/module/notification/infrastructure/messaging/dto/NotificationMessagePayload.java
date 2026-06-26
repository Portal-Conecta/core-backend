package com.portal.conecta.hub.module.notification.infrastructure.messaging.dto;

import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Contrato de entrada para solicitações de notificação recebidas por RabbitMQ.
 *
 * <p>O produtor informa os dados da notificação, os escopos afetados e filtros opcionais.
 * O Hub Core persiste a notificação uma única vez por {@code messageId} e materializa os
 * destinatários em registros de notificação de usuário.</p>
 *
 * <p>O payload não deve carregar credenciais, tokens, cookies, autorização, query string,
 * e-mail ou outros dados sensíveis.</p>
 *
 * @param messageId identificador idempotente da mensagem gerada pelo produtor.
 * @param correlationId identificador de correlação para rastreabilidade entre sistemas.
 * @param type tipo funcional da notificação.
 * @param title título apresentado ao destinatário.
 * @param body conteúdo textual da notificação.
 * @param linkUrl URL opcional associada à ação da notificação.
 * @param publishedAt data de publicação informada pelo produtor.
 * @param filters filtros opcionais aplicados à resolução de destinatários.
 * @param scopes escopos obrigatórios usados para resolver destinatários.
 * @param metadata metadados auxiliares do evento, sem dados sensíveis.
 */
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

        /**
         * Limita quem recebe a notificação dentro do escopo (ex: ROLE=STUDENT).
         * Pode ser vazia apenas quando o escopo for envio direto para um usuário.
         */
        @Valid
        List<NotificationFilterPayload> filters,

        /**
         * Define as entidades afetadas pela notificação (ex: Turma, Curso, Usuário, Sala).
         * Representa o contexto do evento e não necessariamente o usuário final.
         */
        @NotEmpty(message = "Pelo menos um escopo deve ser informado.")
        @Valid
        List<NotificationScopePayload> scope,

        /**
         * Dados auxiliares e não sensíveis para dar contexto à notificação (ex: classId, route).
         * Útil para o front-end montar links de navegação ou debug.
         */
        Map<String, Object> metadata
) {
    /**
     * Converte o payload de mensageria para o comando interno de aplicação.
     *
     * <p>Filtros ausentes são tratados como lista vazia e metadados ausentes como mapa vazio,
     * preservando a obrigatoriedade de ao menos um escopo.</p>
     *
     * @return comando usado pelo caso de uso de processamento.
     */
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
