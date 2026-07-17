package com.portal.conecta.hub.module.notification.infrastructure.messaging.dto.filter;

/**
 * Tipos de filtro aceitos na distribuição de notificações.
 */
public enum NotificationFilterType {
    /**
     * Restringe destinatários pelo tipo global de usuário.
     */
    ROLE,
    SHIFT
}
