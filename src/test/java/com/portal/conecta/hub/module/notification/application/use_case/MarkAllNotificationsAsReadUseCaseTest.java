package com.portal.conecta.hub.module.notification.application.use_case;

import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkAllNotificationsAsReadUseCaseTest {

    @Mock
    private UserNotificationRepository repository;

    @Mock
    private RequestContextProvider contextProvider;

    @InjectMocks
    private MarkAllNotificationsAsReadUseCase useCase;

    @Test
    @DisplayName("deve marcar todas as notificacoes pendentes como lidas em uma query")
    void shouldMarkAllAsReadWithSingleBulkQuery() {
        UUID userId = UUID.randomUUID();

        RequestContext context = mock(RequestContext.class);
        when(context.userId()).thenReturn(userId);
        when(contextProvider.getRequestContext()).thenReturn(context);

        useCase.execute();

        verify(contextProvider).getRequestContext();
        verify(repository).readAllNotificationByUserId(userId);
        verify(repository, never()).findAllByUserIdAndReadAtIsNull(any());
        verify(repository, never()).saveAll(any());
    }

    @Test
    @DisplayName("deve delegar para a query em lote mesmo sem carregar entidades")
    void shouldDelegateToBulkQueryWithoutLoadingEntities() {
        UUID userId = UUID.randomUUID();

        RequestContext context = mock(RequestContext.class);
        when(context.userId()).thenReturn(userId);
        when(contextProvider.getRequestContext()).thenReturn(context);

        useCase.execute();

        verify(contextProvider).getRequestContext();
        verify(repository).readAllNotificationByUserId(userId);
        verify(repository, never()).findAllByUserIdAndReadAtIsNull(any());
        verify(repository, never()).saveAll(any());
    }
}
