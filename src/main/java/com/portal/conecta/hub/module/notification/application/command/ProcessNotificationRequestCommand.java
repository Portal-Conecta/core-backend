package com.portal.conecta.hub.module.notification.application.command;

import com.portal.conecta.hub.module.notification.domain.exception.InvalidNotificationPayloadException;
import com.portal.conecta.hub.module.notification.domain.model.NotificationFilterType;
import com.portal.conecta.hub.module.notification.domain.model.NotificationScopeType;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Comando interno que representa uma solicitação válida de processamento de notificação.
 *
 * <p>O comando é a fronteira entre o contrato de mensageria e a aplicação. Ele garante os
 * campos mínimos da notificação e exige ao menos um escopo para que a resolução de
 * destinatários possa ser executada.</p>
 *
 * @param messageId identificador idempotente da mensagem externa.
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
public record ProcessNotificationRequestCommand(
        String messageId,
        String correlationId,
        String source,
        String eventType,
        Instant occurredAt,
        String title,
        String body,
        List<CommandFilter> filters,
        List<CommandScope> scopes,
        Map<String, Object> metadata
) {
    public ProcessNotificationRequestCommand {
        requireNonBlank(messageId, "messageId");
        requireNonBlank(source, "source");
        requireNonBlank(eventType, "eventType");
        requireNonNull(occurredAt, "occurredAt");
        requireNonBlank(title, "title");
        requireNonBlank(body, "body");
        if (scopes == null || scopes.isEmpty()) {
            throw new InvalidNotificationPayloadException("scope não pode ser vazio.");
        }
    }

    /**
     * Filtro de distribuição aplicado durante a resolução de destinatários.
     *
     * @param type tipo de filtro suportado.
     * @param value valor esperado pelo filtro, como o nome de um {@code TypeUser} para ROLE.
     */
    public record CommandFilter(NotificationFilterType type, String value) {}

    /**
     * Escopo de distribuição informado pela mensagem externa.
     *
     * @param type tipo de entidade usada para resolver destinatários.
     * @param correlationId identificador externo ou UUID da entidade do escopo.
     */
    public record CommandScope(NotificationScopeType type, String correlationId) {}

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new InvalidNotificationPayloadException("Campo obrigatório ausente: " + field);
        }
    }

    private static void requireNonNull(Object value, String field) {
        if (value == null) {
            throw new InvalidNotificationPayloadException("Campo obrigatório ausente: " + field);
        }
    }
}
