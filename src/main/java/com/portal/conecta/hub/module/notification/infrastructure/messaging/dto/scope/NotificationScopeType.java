package com.portal.conecta.hub.module.notification.infrastructure.messaging.dto.scope;

/**
 * Escopos aceitos no contrato de distribuição de notificações.
 *
 * <p>{@code USER} representa entrega direta. {@code CLASS} e {@code COURSE} resolvem
 * destinatários a partir de vínculos acadêmicos. {@code GLOBAL} distribui para usuários
 * ativos do Core. {@code ROOM} é aceito no payload, mas não materializa destinatários
 * nesta implementação.</p>
 */
public enum NotificationScopeType {
    CLASS,
    COURSE,
    ROOM,
    USER,
    GLOBAL
}
