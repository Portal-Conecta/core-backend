package com.portal.conecta.hub.module.notification.infrastructure.resolver;

import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalScopeResolver")
class GlobalScopeResolverTest {

    @Mock
    private UserNotificationRepository userNotificationRepository;

    @InjectMocks
    private GlobalScopeResolver resolver;

    @Test
    @DisplayName("deve inserir notificacao global para todos os usuarios ativos quando nao ha filtro ROLE")
    void deveInserirSemFiltroRole() {
        UUID notificationId = UUID.randomUUID();

        resolver.insert(notificationId, EnumSet.noneOf(TypeUser.class));

        verify(userNotificationRepository).insertByGlobalScope(notificationId);
        verify(userNotificationRepository, never()).insertByGlobalScopeFilteredByRole(notificationId, Set.of());
    }

    @Test
    @DisplayName("deve inserir notificacao global restringindo por ROLE quando filtro e informado")
    void deveInserirComFiltroRole() {
        UUID notificationId = UUID.randomUUID();

        resolver.insert(notificationId, EnumSet.of(TypeUser.STUDENT, TypeUser.TEACHER));

        verify(userNotificationRepository).insertByGlobalScopeFilteredByRole(
                notificationId,
                Set.of(TypeUser.STUDENT.name(), TypeUser.TEACHER.name())
        );
        verify(userNotificationRepository, never()).insertByGlobalScope(notificationId);
    }
}
