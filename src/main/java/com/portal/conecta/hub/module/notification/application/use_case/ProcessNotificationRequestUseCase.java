package com.portal.conecta.hub.module.notification.application.use_case;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand;
import com.portal.conecta.hub.module.notification.domain.exception.InvalidNotificationPayloadException;
import com.portal.conecta.hub.module.notification.domain.model.NotificationEntity;
import com.portal.conecta.hub.module.notification.domain.model.UserNotificationEntity;
import com.portal.conecta.hub.module.notification.domain.port.NotificationRecipientPort;
import com.portal.conecta.hub.module.notification.domain.port.NotificationRepository;
import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class ProcessNotificationRequestUseCase {

    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final NotificationRecipientPort recipientPort;

    public ProcessNotificationRequestUseCase(NotificationRepository notificationRepository, UserNotificationRepository userNotificationRepository, NotificationRecipientPort recipientPort) {
        this.notificationRepository = notificationRepository;
        this.userNotificationRepository = userNotificationRepository;
        this.recipientPort = recipientPort;
    }

    @Transactional
    public NotificationEntity execute (ProcessNotificationRequestCommand command){
        validate (command);

        NotificationEntity notification = notificationRepository.findByMessageId(command.messageId())
                .orElseGet(()-> notificationRepository.save(
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

        Set<UUID> associated = new LinkedHashSet<>();

        for (ProcessNotificationRequestCommand.CommandScope scope : command.scopes()){
            List<UserEntity> recipients = recipientPort.resolve(scope, command.filters());

            for (UserEntity user : recipients){
                if (associated.contains(user.getId())){
                    continue;
                }
                associated.add(user.getId());

                boolean alreadExists = userNotificationRepository.existsByNotificationIdAndUserId(notification.getId(), user.getId());

                if (!alreadExists){
                    userNotificationRepository.save(
                            UserNotificationEntity.create(notification, user)
                    );
                }
            }
        }
        return notification;
    }

    private void validate (ProcessNotificationRequestCommand command){
        requireNonBlank(command.messageId(), "messageId");
        requireNonBlank(command.source(), "source");
        requireNonBlank(command.eventType(), "eventType");
        requireNonNull(command.occurredAt(), "occurredAt");
        requireNonBlank(command.title(), "tittle");
        requireNonBlank(command.body(), "body");

        if (command.scopes() == null || command.scopes().isEmpty()){
            throw new InvalidNotificationPayloadException("scope não pode ser vazio.");
        }
    }

    private void requireNonBlank(String value, String field){
        if (value == null || value.isBlank()){
            throw new InvalidNotificationPayloadException("Campo obrigatório ausente: "+field);
        }
    }
    private void requireNonNull(Object value, String field){
        if (value == null){
            throw new InvalidNotificationPayloadException("Campo obrigatório ausente: "+field);
        }
    }

    private String serializeMetadata(Map<String, Object> metadata){
        if (metadata == null || metadata.isEmpty()) return null;
        try{
            return new ObjectMapper().writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}