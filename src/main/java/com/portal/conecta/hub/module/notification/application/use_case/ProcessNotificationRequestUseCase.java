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


    @Transactional
    public NotificationEntity execute(ProcessNotificationRequestCommand command) {
        NotificationEntity notification = notificationRepository
                .findByMessageId(command.messageId())
                .orElseGet(() -> notificationRepository.save(
                        NotificationEntity.create(
                                command.messageId(),
                                command.correlationId(),
                                command.source(),
                                command.eventType(),
                                command.occurredAt(),
                                command.title(),
                                command.body(),
                                serializeMetadata(command.metadata())
                        )
                ));

        recipientPort.dispatch(notification, command.scopes(), command.filters());

        return notification;
    }

    private JsonNode serializeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) return null;
        try {
            return jsonMapper.valueToTree(metadata);
        } catch (Exception e) {
            log.warn("Falha ao serializar os metadados do evento para JsonNode. Os metadados serão ignorados. Dados recebidos: {}", metadata, e);
            return null;
        }
    }

}