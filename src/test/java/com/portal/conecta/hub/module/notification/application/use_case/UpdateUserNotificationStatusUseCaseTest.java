package com.portal.conecta.hub.module.notification.application.use_case;

import com.portal.conecta.hub.module.notification.aplication.use_case.UpdateUserNotificationStatusUseCase;
import com.portal.conecta.hub.module.notification.domain.exception.NotificationNotFoundException;
import com.portal.conecta.hub.module.notification.domain.model.UserNotificationEntity;
import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUserNotificationStatusUseCaseTest {

    @Mock
    private UserNotificationRepository repository;

    @InjectMocks
    private UpdateUserNotificationStatusUseCase useCase;

    private UUID userId;
    private UUID notificationId;
    private UserNotificationEntity userNotification;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        notificationId = UUID.randomUUID();
        userNotification = mock(UserNotificationEntity.class);
    }

    @Test
    @DisplayName("deve marcar notificação como lida com sucesso")
    void shouldMarkNotificationAsReadSuccessfully() {
        when(repository.findByUserIdAndNotificationId(userId, notificationId))
                .thenReturn(Optional.of(userNotification));

        useCase.markAsRead(userId, notificationId);

        verify(userNotification).markAsRead();
        verify(repository).save(userNotification);
    }

    @Test
    @DisplayName("deve lançar NotificationNotFoundException quando notificação não existe ao marcar como lida")
    void shouldThrowWhenNotificationNotFoundOnMarkAsRead() {
        when(repository.findByUserIdAndNotificationId(userId, notificationId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.markAsRead(userId, notificationId))
                .isInstanceOf(NotificationNotFoundException.class)
                .hasMessageContaining("Notificação não encontrada para o usuário informado.");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("deve ocultar notificação com sucesso")
    void shouldDismissNotificationSuccessfully() {
        when(repository.findByUserIdAndNotificationId(userId, notificationId))
                .thenReturn(Optional.of(userNotification));

        useCase.dismiss(userId, notificationId);

        verify(userNotification).dismiss();
        verify(repository).save(userNotification);
    }

    @Test
    @DisplayName("deve lançar NotificationNotFoundException quando notificação não existe ao ocultar")
    void shouldThrowWhenNotificationNotFoundOnDismiss() {
        when(repository.findByUserIdAndNotificationId(userId, notificationId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.dismiss(userId, notificationId))
                .isInstanceOf(NotificationNotFoundException.class)
                .hasMessageContaining("Notificação não encontrada para o usuário informado.");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("deve marcar todas as notificações visíveis como lidas com sucesso")
    void shouldMarkAllVisibleNotificationsAsReadSuccessfully() {
        UserNotificationEntity mockEntity1 = mock(UserNotificationEntity.class);
        UserNotificationEntity mockEntity2 = mock(UserNotificationEntity.class);

        when(repository.findAllVisibleAndUnreadByUserId(userId))
                .thenReturn(List.of(mockEntity1, mockEntity2));

        useCase.markAllAsRead(userId);

        verify(mockEntity1).markAsRead();
        verify(mockEntity2).markAsRead();
        verify(repository).saveAll(anyList());
    }

    @Test
    @DisplayName("não deve salvar nada quando não houver notificações para marcar como lidas")
    void shouldNotSaveAnythingWhenNoNotificationsToMarkAsRead() {
        when(repository.findAllVisibleAndUnreadByUserId(userId))
                .thenReturn(Collections.emptyList());

        useCase.markAllAsRead(userId);

        verify(repository, never()).saveAll(anyList());
    }
}