package com.portal.conecta.hub.module.notification.infrastructure.resolver;

import com.portal.conecta.hub.module.classes.domain.model.Shift;
import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClassScopeResolverTest {
    @Mock UserNotificationRepository repository;

    @Test void ignoresEmptyScope() {
        new ClassScopeResolver(repository).insert(UUID.randomUUID(), List.of(), EnumSet.noneOf(TypeUser.class), EnumSet.noneOf(Shift.class));
        verify(repository, never()).insertByClassScope(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyList(), org.mockito.ArgumentMatchers.anySet(), org.mockito.ArgumentMatchers.anySet());
    }

    @Test void usesDomainDefaultsWhenFiltersAreAbsent() {
        UUID notificationId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        new ClassScopeResolver(repository).insert(notificationId, List.of(classId), EnumSet.noneOf(TypeUser.class), EnumSet.noneOf(Shift.class));
        verify(repository).insertByClassScope(notificationId, List.of(classId),
                Set.of("STUDENT", "TEACHER", "REPRESENTATIVE"), Set.of("FULL_AM_PM", "FULL_PM_NT"));
    }

    @Test void appliesFiltersAndBatchesLargeScopes() {
        UUID notificationId = UUID.randomUUID();
        List<UUID> ids = new ArrayList<>();
        for (int i = 0; i < 501; i++) ids.add(UUID.randomUUID());
        new ClassScopeResolver(repository).insert(notificationId, ids, EnumSet.of(TypeUser.REPRESENTATIVE), EnumSet.of(Shift.FULL_AM_PM));
        verify(repository).insertByClassScope(notificationId, ids.subList(0, 500), Set.of("REPRESENTATIVE"), Set.of("FULL_AM_PM"));
        verify(repository).insertByClassScope(notificationId, ids.subList(500, 501), Set.of("REPRESENTATIVE"), Set.of("FULL_AM_PM"));
    }
}
