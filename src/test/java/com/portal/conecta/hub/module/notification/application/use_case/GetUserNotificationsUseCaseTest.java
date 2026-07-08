package com.portal.conecta.hub.module.notification.application.use_case;

import com.portal.conecta.hub.module.notification.domain.model.NotificationEntity;
import com.portal.conecta.hub.module.notification.domain.model.NotificationStatus;
import com.portal.conecta.hub.module.notification.domain.model.UserNotificationEntity;
import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserNotificationsUseCaseTest {

    @Mock
    private UserNotificationRepository repository;

    @Mock
    private RequestContextProvider contextProvider;

    @InjectMocks
    private GetUserNotificationsUseCase useCase;

    @Test
    @DisplayName("deve retornar página de notificações visíveis do usuário autenticado")
    void shouldReturnPagedNotificationsForAuthenticatedUser() {
        UUID userId = UUID.randomUUID();

        RequestContext context = mock(RequestContext.class);
        when(context.userId()).thenReturn(userId);
        when(contextProvider.getRequestContext()).thenReturn(context);

        NotificationEntity notification = mock(NotificationEntity.class);
        UserEntity user = mock(UserEntity.class);
        UserNotificationEntity userNotification = UserNotificationEntity.create(notification, user);

        Page<UserNotificationEntity> page = new PageImpl<>(List.of(userNotification));
        when(repository.findVisibleByUserId(userId, false, PageRequest.of(0, 20)))
                .thenReturn(page);

        Page<UserNotificationEntity> result = useCase.execute(NotificationStatus.READ, 0, 20);

        assertThat(result.getContent()).hasSize(1);
        verify(repository).findVisibleByUserId(userId, false, PageRequest.of(0, 20));
    }

    @Test
    @DisplayName("deve retornar apenas notificações não lidas quando status for UNREAD")
    void shouldReturnOnlyUnreadNotificationsWhenStatusIsUnread() {
        UUID userId = UUID.randomUUID();

        RequestContext context = mock(RequestContext.class);
        when(context.userId()).thenReturn(userId);
        when(contextProvider.getRequestContext()).thenReturn(context);

        Page<UserNotificationEntity> page = new PageImpl<>(List.of());
        when(repository.findVisibleByUserId(userId, true, PageRequest.of(0, 20)))
                .thenReturn(page);

        Page<UserNotificationEntity> result = useCase.execute(NotificationStatus.UNREAD, 0, 20);

        assertThat(result.getContent()).isEmpty();
        verify(repository).findVisibleByUserId(userId, true, PageRequest.of(0, 20));
    }

    @Test
    @DisplayName("deve retornar página vazia quando usuário não tem notificações")
    void shouldReturnEmptyPageWhenUserHasNoNotifications() {
        UUID userId = UUID.randomUUID();

        RequestContext context = mock(RequestContext.class);
        when(context.userId()).thenReturn(userId);
        when(contextProvider.getRequestContext()).thenReturn(context);

        when(repository.findVisibleByUserId(userId, false, PageRequest.of(0, 20)))
                .thenReturn(Page.empty());

        Page<UserNotificationEntity> result = useCase.execute(NotificationStatus.READ, 0, 20);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}
