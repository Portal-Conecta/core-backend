package com.portal.conecta.hub.module.notification.application.use_case;

import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand;
import com.portal.conecta.hub.module.notification.domain.model.NotificationEntity;
import com.portal.conecta.hub.module.notification.domain.port.NotificationRecipientPort;
import com.portal.conecta.hub.module.notification.domain.port.NotificationRepository;
import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

@Component
public class ProcessNotificationRequestUseCase {

    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final NotificationRecipientPort recipientPort;
    private final JsonMapper jsonMapper;

    public ProcessNotificationRequestUseCase(
            NotificationRepository notificationRepository,
            UserNotificationRepository userNotificationRepository,
            NotificationRecipientPort recipientPort,
            JsonMapper jsonMapper) {
        this.notificationRepository = notificationRepository;
        this.userNotificationRepository = userNotificationRepository;
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

            List<UUID> userIds = recipientPort.resolveAll(command.scopes(), command.filters());
            if (!userIds.isEmpty()) {
                userNotificationRepository.insertForUsers(notification.getId(), userIds);
            }

        return notification;
    }

    private String serializeMetadata(java.util.Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) return null;
        try {
            return jsonMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            return null;
        }
    }
}