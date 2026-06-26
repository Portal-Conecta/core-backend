package com.portal.conecta.hub.module.notification.application.use_case;

import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand;
import com.portal.conecta.hub.module.notification.domain.model.NotificationEntity;
import com.portal.conecta.hub.module.notification.domain.port.NotificationRecipientPort;
import com.portal.conecta.hub.module.notification.domain.port.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.Optional;

/**
 * Processa uma solicitação externa de notificação recebida por mensageria.
 *
 * <p>A notificação representa o evento persistido uma única vez por {@code messageId}.
 * As notificações de usuário são materializações individuais criadas a partir dos escopos
 * e filtros informados no comando.</p>
 */
@Component
@Slf4j
public class ProcessNotificationRequestUseCase {

    private final NotificationRepository notificationRepository;
    private final NotificationRecipientPort recipientPort;
    private final JsonMapper jsonMapper;

    public ProcessNotificationRequestUseCase(
            NotificationRepository notificationRepository,
            NotificationRecipientPort recipientPort,
            JsonMapper jsonMapper) {
        this.notificationRepository = notificationRepository;
        this.recipientPort = recipientPort;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Cria ou reutiliza a notificação do evento externo e dispara a resolução de destinatários.
     *
     * @param command comando validado com dados, filtros e escopos da notificação.
     * @return notificação persistida ou reutilizada para o {@code messageId}.
     * @throws com.portal.conecta.hub.module.notification.domain.exception.InvalidNotificationPayloadException
     * quando o comando não possui os campos obrigatórios ou contém escopo/filtro inválido.
     */
    @Transactional
    public NotificationEntity execute(ProcessNotificationRequestCommand command) {
        Optional<NotificationEntity> existing = notificationRepository.findByMessageId(command.messageId());
        NotificationEntity notification;

        if (existing.isPresent()) {
            notification = existing.get();
            log.info("Notificação já existente reutilizada para mensagem externa. notificationId={}, messageId={}",
                    notification.getId(), command.messageId());
        } else {
            notification = notificationRepository.save(
                    NotificationEntity.create(
                            command.messageId(),
                            command.correlationId(),
                            command.source(),
                            command.eventType(),
                            command.occurredAt(),
                            command.title(),
                            command.body(),
                            serializeMetadata(command.messageId(), command.metadata())
                    )
            );
            log.info("Notificação criada a partir de mensagem externa. notificationId={}, messageId={}, source={}, eventType={}",
                    notification.getId(), command.messageId(), command.source(), command.eventType());
        }

        recipientPort.dispatch(notification, command.scopes(), command.filters());

        return notification;
    }

    private JsonNode serializeMetadata(String messageId, Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) return null;
        try {
            return jsonMapper.valueToTree(metadata);
        } catch (Exception e) {
            log.warn("Metadados da notificação ignorados por falha de serialização. messageId={}", messageId, e);
            return null;
        }
    }
}
