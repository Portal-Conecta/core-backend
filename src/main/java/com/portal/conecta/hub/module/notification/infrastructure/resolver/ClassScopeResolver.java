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
 * Resolve destinatários a partir de turmas informadas no escopo CLASS.
 *
 * <p>Quando nenhum filtro ROLE é informado, a distribuição considera estudantes,
 * professores e representantes vinculados à turma ativa.</p>
 */
@Component
public class ClassScopeResolver {

    private static final EnumSet<TypeUser>DEFAULT_TYPES =
            EnumSet.of(TypeUser.STUDENT, TypeUser.TEACHER, TypeUser.REPRESENTATIVE);

    private static final int BATCH_SIZE = 500;

    private final UserNotificationRepository userNotificationRepository;

    public ClassScopeResolver(UserNotificationRepository userNotificationRepository) {
        this.userNotificationRepository = userNotificationRepository;
    }

    /**
     * Cria vínculos de notificação para usuários ativos associados às turmas informadas.
     *
     * @param notificationId identificador da notificação global.
     * @param classIds turmas usadas para resolver usuários vinculados.
     * @param types tipos de usuário permitidos pelo filtro ROLE; vazio usa o padrão do domínio.
     */
    public void insert (UUID notificationId, List<UUID> classIds, EnumSet<TypeUser> types){
        if (classIds.isEmpty()) return;

        EnumSet<TypeUser> effectiveTypes = types.isEmpty() ? DEFAULT_TYPES : types;
        Set<String> typeNames = effectiveTypes.stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        for (int i = 0; i<classIds.size(); i += BATCH_SIZE){
            List<UUID> batch = classIds.subList(i, Math.min(i + BATCH_SIZE, classIds.size()));
            userNotificationRepository.insertByClassScope(notificationId, batch, typeNames);
        }
    }
}
