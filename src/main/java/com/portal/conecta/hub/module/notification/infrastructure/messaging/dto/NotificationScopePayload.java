package com.portal.conecta.hub.module.notification.infrastructure.messaging.dto;

import com.portal.conecta.hub.module.notification.domain.model.NotificationScopeType;
import jakarta.validation.constraints.NotNull;

/**
 * Escopo informado pelo produtor para indicar quais entidades são afetadas pela notificação.
 *
 * <p>Escopos de usuário geram distribuição direta. Escopos de turma e curso são resolvidos
 * para os usuários vinculados, respeitando os filtros aplicáveis. Escopos sem suporte de
 * distribuição podem ser recebidos sem criar destinatários.</p>
 *
 * @param type tipo de entidade usado para resolver destinatários.
 * @param correlationId identificador externo ou UUID da entidade do escopo.
 */
public record NotificationScopePayload(
        @NotNull(message = "O tipo do escopo (type) é obrigatório.")
        NotificationScopeType type,

        String correlationId
) {}
