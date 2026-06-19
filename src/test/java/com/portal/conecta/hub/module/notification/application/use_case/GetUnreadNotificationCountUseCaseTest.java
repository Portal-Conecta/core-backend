package com.portal.conecta.hub.module.notification.application.use_case;

import com.portal.conecta.hub.module.notification.application.usecase.GetUnreadNotificationCountUseCase;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUnreadNotificationCountUseCaseTest {

    @Mock
    private UserNotificationRepository repository;

    @Mock
    private RequestContextProvider contextProvider;

    @InjectMocks
    private GetUnreadNotificationCountUseCase useCase;

    @Test
    @DisplayName("deve retornar contagem de notificações não lidas do usuário autenticado")
    void shouldReturnUnreadCountForAuthenticatedUser() {
        UUID userId = UUID.randomUUID();

        RequestContext context = mock(RequestContext.class);
        when(context.userId()).thenReturn(userId);
        when(contextProvider.getRequestContext()).thenReturn(context);

        when(repository.countUnreadByUserId(userId)).thenReturn(3L);

        long result = useCase.execute();

        assertThat(result).isEqualTo(3L);
        verify(repository).countUnreadByUserId(userId);
    }

    @Test
    @DisplayName("deve retornar zero quando usuário não tem notificações não lidas")
    void shouldReturnZeroWhenNoUnreadNotifications() {
        UUID userId = UUID.randomUUID();

        RequestContext context = mock(RequestContext.class);
        when(context.userId()).thenReturn(userId);
        when(contextProvider.getRequestContext()).thenReturn(context);

        when(repository.countUnreadByUserId(userId)).thenReturn(0L);

        long result = useCase.execute();

        assertThat(result).isZero();
        verify(repository).countUnreadByUserId(userId);
    }
}
