package com.portal.conecta.hub.module.notification.application.use_case;

import com.portal.conecta.hub.module.notification.application.command.MarkAsReadNotificationsCommand;
import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkAsReadNotificationsUseCaseTest {

    @Mock
    private UserNotificationRepository repository;

    @Mock
    private RequestContextProvider contextProvider;

    @InjectMocks
    private MarkAsReadNotificationsUseCase useCase;

    @Test
    @DisplayName("deve marcar lista de notificacoes como lida para o usuario autenticado")
    void shouldMarkNotificationsAsReadForAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        List<UUID> notificationIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        RequestContext context = mock(RequestContext.class);
        when(context.userId()).thenReturn(userId);
        when(contextProvider.getRequestContext()).thenReturn(context);

        useCase.execute(new MarkAsReadNotificationsCommand(notificationIds));

        verify(contextProvider).getRequestContext();
        verify(repository).markAsReadNotifications(notificationIds, userId);
    }
}
