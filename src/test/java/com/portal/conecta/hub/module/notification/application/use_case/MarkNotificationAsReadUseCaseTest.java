package com.portal.conecta.hub.module.notification.application.use_case;

import com.portal.conecta.hub.module.notification.domain.exception.NotificationNotFoundException;
import com.portal.conecta.hub.module.notification.domain.model.NotificationEntity;
import com.portal.conecta.hub.module.notification.domain.model.UserNotificationEntity;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarkNotificationAsReadUseCaseTest {

    @Mock
    private UserNotificationRepository repository;

    @Mock
    private RequestContextProvider contextProvider;

    @InjectMocks
    private MarkNotificationAsReadUseCase useCase;

    @Test
    @DisplayName("deve marcar notificação como lida com sucesso")
    void shouldMarkAsReadSuccessfully() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        NotificationEntity notification = mock(NotificationEntity.class);
        UserEntity user = mock(UserEntity.class);
        UserNotificationEntity userNotification = UserNotificationEntity.create(notification, user);

        RequestContext context = mock(RequestContext.class);
        when(context.userId()).thenReturn(userId);
        when(contextProvider.getRequestContext()).thenReturn(context);

        when(repository.findByUserIdAndNotificationId(userId, notificationId))
                .thenReturn(Optional.of(userNotification));

        useCase.execute(notificationId);

        assertThat(userNotification.isRead()).isTrue();
        assertThat(userNotification.getReadAt()).isNotNull();
        verify(contextProvider).getRequestContext();
        verify(repository).save(userNotification);
    }

    @Test
    @DisplayName("deve lançar exceção quando notificação não for encontrada")
    void shouldThrowExceptionWhenNotificationNotFound() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        RequestContext context = mock(RequestContext.class);
        when(context.userId()).thenReturn(userId);
        when(contextProvider.getRequestContext()).thenReturn(context);

        when(repository.findByUserIdAndNotificationId(userId, notificationId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(notificationId))
                .isInstanceOf(NotificationNotFoundException.class)
                .hasMessage("Notificação não encontrada para o usuário informado.");

        verify(contextProvider).getRequestContext();
        verify(repository, never()).save(any());
    }
}
