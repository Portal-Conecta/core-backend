package com.portal.conecta.hub.module.notification.presentation.controller;

import com.portal.conecta.hub.module.notification.application.use_case.*;
import com.portal.conecta.hub.module.notification.domain.model.NotificationEntity;
import com.portal.conecta.hub.module.notification.domain.model.UserNotificationEntity;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.shared.exception.GlobalExceptionHandler;
import com.portal.conecta.hub.shared.exception.UnauthorizedUserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
        import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private MarkNotificationAsReadUseCase markAsReadUseCase;

    @Mock
    private DismissNotificationUseCase dismissUseCase;

    @Mock
    private MarkAllNotificationsAsReadUseCase markAllAsReadUseCase;

    @Mock
    private GetUserNotificationsUseCase getUserNotificationsUseCase;

    @Mock
    private GetUnreadNotificationCountUseCase getUnreadCountUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new NotificationController(
                        markAsReadUseCase,
                        dismissUseCase,
                        markAllAsReadUseCase,
                        getUserNotificationsUseCase,
                        getUnreadCountUseCase
                ))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private UserNotificationEntity buildUserNotification() {
        NotificationEntity notification = mock(NotificationEntity.class);
        UserEntity user = mock(UserEntity.class);

        UUID notificationId = UUID.randomUUID();
        Instant now = Instant.parse("2026-06-17T20:56:00Z");

        when(notification.getId()).thenReturn(notificationId);
        when(notification.getTitle()).thenReturn("Mapa de sala atualizado");
        when(notification.getBody()).thenReturn("A turma foi reorganizada.");
        when(notification.getSource()).thenReturn("seatmap-service");
        when(notification.getEventType()).thenReturn("seatmap.updated");
        when(notification.getOccurredAt()).thenReturn(now);
        when(notification.getMetadata()).thenReturn(null);

        UserNotificationEntity userNotification = UserNotificationEntity.create(notification, user);
        ReflectionTestUtils.setField(userNotification, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(userNotification, "createdAt", now);

        return userNotification;
    }

    // ==================== GET /notifications ====================

    @Test
    @DisplayName("deve retornar 200 com lista paginada de notificações")
    void shouldReturnPagedNotifications() throws Exception {
        UserNotificationEntity userNotification = buildUserNotification();
        Page<UserNotificationEntity> page = new PageImpl<>(List.of(userNotification));

        when(getUserNotificationsUseCase.execute(false, 0, 20)).thenReturn(page);

        mockMvc.perform(get("/api/v1/notifications")
                        .param("page", "0")
                        .param("size", "20")
                        .param("unreadOnly", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Mapa de sala atualizado"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @DisplayName("deve retornar 200 com lista vazia quando usuário não tem notificações")
    void shouldReturnEmptyPageWhenNoNotifications() throws Exception {
        when(getUserNotificationsUseCase.execute(false, 0, 20)).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/notifications")
                        .param("page", "0")
                        .param("size", "20")
                        .param("unreadOnly", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("deve retornar 200 com apenas não lidas quando unreadOnly=true")
    void shouldReturnOnlyUnreadNotificationsWhenUnreadOnlyIsTrue() throws Exception {
        when(getUserNotificationsUseCase.execute(true, 0, 20)).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/notifications")
                        .param("page", "0")
                        .param("size", "20")
                        .param("unreadOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(getUserNotificationsUseCase).execute(true, 0, 20);
    }

    @Test
    @DisplayName("deve retornar 401 quando não autenticado ao listar notificações")
    void listShouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        when(getUserNotificationsUseCase.execute(anyBoolean(), anyInt(), anyInt()))
                .thenThrow(new UnauthorizedUserException());

        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/v1/notifications"));
    }

    // ==================== GET /notifications/unread-count ====================

    @Test
    @DisplayName("deve retornar 200 com contagem de notificações não lidas")
    void shouldReturnUnreadCount() throws Exception {
        when(getUnreadCountUseCase.execute()).thenReturn(3L);

        mockMvc.perform(get("/api/v1/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(3));
    }

    @Test
    @DisplayName("deve retornar 200 com contagem zero quando não há não lidas")
    void shouldReturnZeroUnreadCount() throws Exception {
        when(getUnreadCountUseCase.execute()).thenReturn(0L);

        mockMvc.perform(get("/api/v1/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(0));
    }

    @Test
    @DisplayName("deve retornar 401 quando não autenticado ao consultar contador")
    void unreadCountShouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        when(getUnreadCountUseCase.execute())
                .thenThrow(new UnauthorizedUserException());

        mockMvc.perform(get("/api/v1/notifications/unread-count"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/v1/notifications/unread-count"));
    }
}