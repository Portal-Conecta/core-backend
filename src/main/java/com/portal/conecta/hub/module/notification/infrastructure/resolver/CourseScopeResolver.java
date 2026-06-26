package com.portal.conecta.hub.module.notification.infrastructure.resolver;

import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Resolve destinatários a partir de cursos informados no escopo COURSE.
 *
 * <p>Quando nenhum filtro ROLE é informado, a distribuição considera estudantes,
 * professores e representantes vinculados a turmas ativas do curso.</p>
 */
@Component
public class CourseScopeResolver {

    private static final EnumSet<TypeUser> DEFAULT_TYPES =
            EnumSet.of(TypeUser.STUDENT, TypeUser.TEACHER, TypeUser.REPRESENTATIVE);

    private static final int BATCH_SIZE = 500;

    private final UserNotificationRepository userNotificationRepository;

    public CourseScopeResolver(UserNotificationRepository userNotificationRepository) {
        this.userNotificationRepository = userNotificationRepository;
    }

    /**
     * Cria vínculos de notificação para usuários ativos associados aos cursos informados.
     *
     * @param notificationId identificador da notificação global.
     * @param courseIds cursos usados para resolver turmas e usuários vinculados.
     * @param types tipos de usuário permitidos pelo filtro ROLE; vazio usa o padrão do domínio.
     */
    public void insert (UUID notificationId, List<UUID> courseIds, EnumSet<TypeUser> types){
        if (courseIds.isEmpty()) return;

        EnumSet<TypeUser> effectiveTypes = types.isEmpty() ? DEFAULT_TYPES : types;
        Set<String> typeNames = effectiveTypes.stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        for (int i = 0; i < courseIds.size(); i += BATCH_SIZE){

            List<UUID> batch = courseIds.subList(i, Math.min(i + BATCH_SIZE, courseIds.size()));
            userNotificationRepository.insertByCourseScope(notificationId, batch, typeNames);

        }
    }
}
