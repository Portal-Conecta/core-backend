package com.portal.conecta.hub.module.notification.application.use_case;

import com.portal.conecta.hub.module.notification.domain.model.NotificationEntity;
import com.portal.conecta.hub.module.notification.domain.model.UserNotificationEntity;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarkAllNotificationsAsReadUseCaseTest {

    @Mock
    private UserNotificationRepository repository;

    @InjectMocks
    private MarkAllNotificationsAsReadUseCase useCase;

    @Test
    @DisplayName("deve marcar todas as notificações pendentes como lidas em lote")
    void shouldMarkAllAsReadSuccessfully() {
        UUID userId = UUID.randomUUID();

        NotificationEntity notification = mock(NotificationEntity.class);
        UserEntity user = mock(UserEntity.class);

        UserNotificationEntity userNotification1 = UserNotificationEntity.create(notification, user);
        UserNotificationEntity userNotification2 = UserNotificationEntity.create(notification, user);

        List<UserNotificationEntity> unreadList = List.of(userNotification1, userNotification2);

        when(repository.findAllByUserIdAndReadAtIsNull(userId)).thenReturn(unreadList);

        useCase.execute(userId);

        assertThat(userNotification1.isRead()).isTrue();
        assertThat(userNotification2.isRead()).isTrue();
        verify(repository).saveAll(unreadList);
    }

    @Test
    @DisplayName("não deve fazer nada quando não há notificações pendentes")
    void shouldDoNothingWhenNoUnreadNotifications() {
        UUID userId = UUID.randomUUID();

        when(repository.findAllByUserIdAndReadAtIsNull(userId)).thenReturn(List.of());

        useCase.execute(userId);

        verify(repository).saveAll(List.of());
    }
}
