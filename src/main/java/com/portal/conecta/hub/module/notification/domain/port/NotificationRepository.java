package com.portal.conecta.hub.module.notification.domain.port;

import com.portal.conecta.hub.module.notification.domain.model.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Porta de persistência da notificação global.
 *
 * <p>O {@code messageId} identifica a mensagem externa e é usado para evitar criação
 * duplicada da mesma notificação quando houver reprocessamento.</p>
 */
public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    /**
     * Busca uma notificação pelo identificador da mensagem externa.
     *
     * @param messageId identificador idempotente recebido por mensageria.
     * @return notificação existente, quando já processada.
     */
    Optional<NotificationEntity> findByMessageId(String messageId);
}
