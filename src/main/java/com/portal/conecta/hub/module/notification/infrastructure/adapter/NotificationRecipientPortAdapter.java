package com.portal.conecta.hub.module.notification.infrastructure.adapter;

import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand;
import com.portal.conecta.hub.module.notification.domain.exception.InvalidNotificationPayloadException;
import com.portal.conecta.hub.module.notification.domain.model.NotificationEntity;
import com.portal.conecta.hub.module.notification.domain.port.NotificationRecipientPort;
import com.portal.conecta.hub.module.notification.infrastructure.resolver.ClassScopeResolver;
import com.portal.conecta.hub.module.notification.infrastructure.resolver.CourseScopeResolver;
import com.portal.conecta.hub.module.notification.infrastructure.resolver.UserDirectResolver;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
@Profile({"dev", "prod"})
public class NotificationRecipientPortAdapter implements NotificationRecipientPort {


    private final UserDirectResolver userDirectResolver;
    private final ClassScopeResolver classScopeResolver;
    private final CourseScopeResolver courseScopeResolver;

    public NotificationRecipientPortAdapter(UserDirectResolver userDirectResolver, ClassScopeResolver classScopeResolver, CourseScopeResolver courseScopeResolver) {
        this.userDirectResolver = userDirectResolver;
        this.classScopeResolver = classScopeResolver;
        this.courseScopeResolver = courseScopeResolver;
    }

    @Override
    public void dispatch(NotificationEntity notification, List<ProcessNotificationRequestCommand.CommandScope> scopes,
                         List<ProcessNotificationRequestCommand.CommandFilter> filters) {

        UUID notificationId = notification.getId();

        EnumSet<TypeUser> roleTypes = EnumSet.noneOf(TypeUser.class);

        for (ProcessNotificationRequestCommand.CommandFilter filter : filters) {
            if ("ROLE".equals(filter.type().name())) {
                try {
                    roleTypes.add(TypeUser.valueOf(filter.value()));
                } catch (IllegalArgumentException e) {
                    throw new InvalidNotificationPayloadException("Invalid ROLE filter value: " + filter.value());
                }
            }
        }

        Set<UUID> userIds = new LinkedHashSet<>();
        List<UUID> classIds = new ArrayList<>();
        List<UUID> courseIds = new ArrayList<>();

        for (ProcessNotificationRequestCommand.CommandScope scope : scopes) {
            switch (scope.type()) {
                case USER -> {
                    UUID id = parseUuid(scope.correlationId());
                    userIds.add(id);
                }
                case CLASS -> {
                    UUID id = parseUuid(scope.correlationId());
                    classIds.add(id);
                }
                case COURSE -> {
                    UUID id = parseUuid(scope.correlationId());
                    courseIds.add(id);
                }
                case ROOM, GLOBAL ->
                        log.warn("Escopo de notificação ignorado por tipo não suportado. scopeType={}",
                                scope.type());
                default ->
                        log.warn("Tipo de escopo de notificação desconhecido. scopeType={}",
                                scope.type());
            }
        }

        userDirectResolver.insert(notificationId, userIds);
        classScopeResolver.insert(notificationId, classIds, roleTypes);
        courseScopeResolver.insert(notificationId, courseIds, roleTypes);

        log.info("Destinatários de notificação resolvidos. notificationId={}, userCount={}, classCount={}, courseCount={}",
                notificationId, userIds.size(), classIds.size(), courseIds.size());
    }

    private UUID parseUuid(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            throw new InvalidNotificationPayloadException(
                    "Invalid UUID in scope correlationId: " + raw
            );
        }
    }
}