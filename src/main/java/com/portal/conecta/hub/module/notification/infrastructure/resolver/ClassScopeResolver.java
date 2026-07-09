package com.portal.conecta.hub.module.notification.infrastructure.resolver;

import com.portal.conecta.hub.module.classes.domain.model.Shift;
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
 * professores e representantes vinculados à turma ativa. Quando nenhum filtro SHIFT
 * é informado, todos os turnos de turmas ativas são considerados.</p>
 */
@Component
public class ClassScopeResolver {

    private static final EnumSet<TypeUser> DEFAULT_TYPES =
            EnumSet.of(TypeUser.STUDENT, TypeUser.TEACHER, TypeUser.REPRESENTATIVE);

    private static final EnumSet<Shift> DEFAULT_SHIFTS = EnumSet.allOf(Shift.class);

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
     * @param shifts turnos de turma permitidos pelo filtro SHIFT; vazio considera todos os turnos.
     */
    public void insert(UUID notificationId, List<UUID> classIds, EnumSet<TypeUser> types, EnumSet<Shift> shifts) {
        if (classIds.isEmpty()) return;

        EnumSet<TypeUser> effectiveTypes = types.isEmpty() ? DEFAULT_TYPES : types;
        Set<String> typeNames = effectiveTypes.stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        EnumSet<Shift> effectiveShifts = shifts.isEmpty() ? DEFAULT_SHIFTS : shifts;
        Set<String> shiftNames = effectiveShifts.stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        for (int i = 0; i < classIds.size(); i += BATCH_SIZE) {
            List<UUID> batch = classIds.subList(i, Math.min(i + BATCH_SIZE, classIds.size()));
            userNotificationRepository.insertByClassScope(notificationId, batch, typeNames, shiftNames);
        }
    }
}