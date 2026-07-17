package com.portal.conecta.hub.module.notification.infrastructure.adapter;

import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand;
import com.portal.conecta.hub.module.notification.domain.exception.InvalidNotificationPayloadException;
import com.portal.conecta.hub.module.notification.domain.model.NotificationEntity;
import com.portal.conecta.hub.module.notification.domain.port.NotificationRecipientPort;
import com.portal.conecta.hub.module.notification.infrastructure.resolver.ClassScopeResolver;
import com.portal.conecta.hub.module.notification.infrastructure.resolver.CourseScopeResolver;
import com.portal.conecta.hub.module.notification.infrastructure.resolver.GlobalScopeResolver;
import com.portal.conecta.hub.module.notification.infrastructure.resolver.NotificationRecipientFilterResolver;
import com.portal.conecta.hub.module.notification.infrastructure.resolver.NotificationRecipientFilters;
import com.portal.conecta.hub.module.notification.infrastructure.resolver.UserDirectResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Implementação de distribuição de notificações baseada nos escopos suportados pelo Hub Core.
 *
 * <p>Escopos {@code USER} geram entrega direta. Escopos {@code CLASS} e {@code COURSE}
 * resolvem usuários vinculados às turmas ou cursos. Escopos {@code GLOBAL} resolvem usuários
 * ativos do Core. Os filtros {@code ROLE} e {@code SHIFT}, quando presentes, restringem a
 * distribuição pelo tipo global do usuário e pelo turno da turma, respectivamente. O filtro
 * {@code ROLE} também se aplica ao escopo {@code GLOBAL}; {@code SHIFT} se aplica apenas aos
 * escopos {@code CLASS} e {@code COURSE}. O escopo {@code USER} nunca é filtrado por ROLE ou
 * SHIFT.</p>
 */
@Component
@Slf4j
@Profile({"dev", "prod"})
public class NotificationRecipientPortAdapter implements NotificationRecipientPort {


    private final UserDirectResolver userDirectResolver;
    private final ClassScopeResolver classScopeResolver;
    private final CourseScopeResolver courseScopeResolver;
    private final GlobalScopeResolver globalScopeResolver;
    private final NotificationRecipientFilterResolver filterResolver;

    public NotificationRecipientPortAdapter(UserDirectResolver userDirectResolver, ClassScopeResolver classScopeResolver,
                                            CourseScopeResolver courseScopeResolver, GlobalScopeResolver globalScopeResolver,
                                            NotificationRecipientFilterResolver filterResolver) {
        this.userDirectResolver = userDirectResolver;
        this.classScopeResolver = classScopeResolver;
        this.courseScopeResolver = courseScopeResolver;
        this.globalScopeResolver = globalScopeResolver;
        this.filterResolver = filterResolver;
    }

    /**
     * Resolve destinatários e cria vínculos de notificação de usuário.
     *
     * <p>Escopo {@code GLOBAL} distribui para usuários ativos e não exige correlationId.
     * Escopo {@code ROOM} é aceito no payload, mas não cria destinatários nesta
     * implementação.</p>
     *
     * @param notification notificação global persistida.
     * @param scopes escopos informados pelo produtor.
     * @param filters filtros opcionais de distribuição.
     * @throws InvalidNotificationPayloadException quando um filtro ROLE, SHIFT ou UUID de escopo é inválido.
     */
    @Override
    public void dispatch(NotificationEntity notification, List<ProcessNotificationRequestCommand.CommandScope> scopes,
                         List<ProcessNotificationRequestCommand.CommandFilter> filters) {

        UUID notificationId = notification.getId();

        NotificationRecipientFilters recipientFilters = filterResolver.resolve(filters);

        Set<UUID> userIds = new LinkedHashSet<>();
        List<UUID> classIds = new ArrayList<>();
        List<UUID> courseIds = new ArrayList<>();
        boolean hasGlobalScope = false;

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
                case GLOBAL -> hasGlobalScope = true;
                case ROOM ->
                        log.warn("Escopo de notificação ignorado por tipo não suportado. scopeType={}",
                                scope.type());
                default ->
                        log.warn("Tipo de escopo de notificação desconhecido. scopeType={}",
                                scope.type());
            }
        }

        userDirectResolver.insert(notificationId, userIds);
        classScopeResolver.insert(notificationId, classIds, recipientFilters.roleTypes(), recipientFilters.shifts());
        courseScopeResolver.insert(notificationId, courseIds, recipientFilters.roleTypes(), recipientFilters.shifts());
        if (hasGlobalScope) {
            globalScopeResolver.insert(notificationId, recipientFilters.roleTypes());
        }

        log.info("Destinatários de notificação resolvidos. notificationId={}, userCount={}, classCount={}, courseCount={}, globalScope={}",
                notificationId, userIds.size(), classIds.size(), courseIds.size(), hasGlobalScope);
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
