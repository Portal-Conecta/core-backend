package com.portal.conecta.hub.module.notification.domain.model;

/**
 * Escopos aceitos no contrato de distribuição de notificações.
 *
 * <p>{@code USER} representa entrega direta. {@code CLASS} e {@code COURSE} resolvem
 * destinatários a partir de vínculos acadêmicos. {@code ROOM} e {@code GLOBAL} são aceitos
 * no payload, mas não materializam destinatários nesta implementação.</p>
 */
public enum NotificationScopeType {
    CLASS,
    COURSE,
    ROOM,
    USER,
    GLOBAL
}
