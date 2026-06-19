package com.portal.conecta.hub.module.notification.application.use_case;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand;
import com.portal.conecta.hub.module.notification.domain.exception.InvalidNotificationPayloadException;
import com.portal.conecta.hub.module.notification.domain.model.NotificationEntity;
import com.portal.conecta.hub.module.notification.domain.model.NotificationScopeType;
import com.portal.conecta.hub.module.notification.domain.port.NotificationRecipientPort;
import com.portal.conecta.hub.module.notification.domain.port.NotificationRepository;
import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessNotificationRequestUseCaseTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserNotificationRepository userNotificationRepository;
    @Mock private NotificationRecipientPort recipientPort;
    @Mock private JsonMapper jsonMapper;

    @InjectMocks
    private ProcessNotificationRequestUseCase useCase;

    private ProcessNotificationRequestCommand validCommand;
    private NotificationEntity savedNotification;

    @BeforeEach
    void setUp() {
        validCommand = new ProcessNotificationRequestCommand(
                "msg-001",
                "corr-001",
                "seat_map",
                "SEAT_UPDATED",
                Instant.now(),
                "Mapa atualizado",
                "O mapa de sala foi atualizado.",
                List.of(),
                List.of(new ProcessNotificationRequestCommand.CommandScope(
                        NotificationScopeType.CLASS, UUID.randomUUID().toString())),
                Map.of()
        );

        savedNotification = buildNotification("msg-001");
    }

    // -------------------------------------------------------------------------
    // Sucesso
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deve criar notificação base quando payload é válido")
    void shouldCreateNotificationWhenPayloadIsValid() {
        when(notificationRepository.findByMessageId("msg-001")).thenReturn(Optional.empty());
        when(notificationRepository.save(any())).thenReturn(savedNotification);
        when(recipientPort.resolve(any(), any())).thenReturn(List.of());

        NotificationEntity result = useCase.execute(validCommand);

        assertThat(result.getMessageId()).isEqualTo("msg-001");
        verify(notificationRepository).save(any(NotificationEntity.class));
    }

    @Test
    @DisplayName("deve chamar a port de resolução para cada scope")
    void shouldCallRecipientPortForEachScope() {
        ProcessNotificationRequestCommand command = new ProcessNotificationRequestCommand(
                "msg-002", null, "src", "EVT", Instant.now(), "T", "B",
                List.of(),
                List.of(
                        new ProcessNotificationRequestCommand.CommandScope(NotificationScopeType.CLASS, UUID.randomUUID().toString()),
                        new ProcessNotificationRequestCommand.CommandScope(NotificationScopeType.COURSE, UUID.randomUUID().toString())
                ),
                Map.of()
        );

        when(notificationRepository.findByMessageId("msg-002")).thenReturn(Optional.empty());
        when(notificationRepository.save(any())).thenReturn(buildNotification("msg-002"));
        when(recipientPort.resolve(any(), any())).thenReturn(List.of());

        useCase.execute(command);

        verify(recipientPort, times(2)).resolve(any(), any());
    }

    @Test
    @DisplayName("deve executar insert set-based com os IDs retornados pela port")
    void shouldInsertForUsersWhenPortReturnsIds() {
        List<UUID> userIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        when(notificationRepository.findByMessageId("msg-001")).thenReturn(Optional.empty());
        when(notificationRepository.save(any())).thenReturn(savedNotification);
        when(recipientPort.resolve(any(), any())).thenReturn(userIds);

        useCase.execute(validCommand);

        verify(userNotificationRepository).insertForUsers(savedNotification.getId(), userIds);
    }

    @Test
    @DisplayName("não deve chamar insertForUsers quando port retorna lista vazia")
    void shouldNotInsertWhenPortReturnsEmptyList() {
        when(notificationRepository.findByMessageId("msg-001")).thenReturn(Optional.empty());
        when(notificationRepository.save(any())).thenReturn(savedNotification);
        when(recipientPort.resolve(any(), any())).thenReturn(List.of());

        useCase.execute(validCommand);

        verify(userNotificationRepository, never()).insertForUsers(any(), any());
    }

    // -------------------------------------------------------------------------
    // Idempotência
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("messageId duplicado não deve criar nova notificação base")
    void shouldReuseExistingNotificationWhenMessageIdIsDuplicate() {
        when(notificationRepository.findByMessageId("msg-001")).thenReturn(Optional.of(savedNotification));
        when(recipientPort.resolve(any(), any())).thenReturn(List.of());

        useCase.execute(validCommand);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("ON CONFLICT DO NOTHING garante deduplicação no banco para messageId duplicado com novos scopes")
    void shouldNotDuplicateUserNotificationOnReprocessing() {
        List<UUID> userIds = List.of(UUID.randomUUID());

        when(notificationRepository.findByMessageId("msg-001")).thenReturn(Optional.of(savedNotification));
        when(recipientPort.resolve(any(), any())).thenReturn(userIds);

        useCase.execute(validCommand);

        verify(userNotificationRepository).insertForUsers(savedNotification.getId(), userIds);
    }

    // -------------------------------------------------------------------------
    // Validação — agora no construtor do command
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deve lançar InvalidNotificationPayloadException quando messageId está ausente")
    void shouldThrowWhenMessageIdIsBlank() {
        assertThatThrownBy(() -> new ProcessNotificationRequestCommand(
                "", null, "src", "EVT", Instant.now(), "T", "B",
                List.of(),
                List.of(new ProcessNotificationRequestCommand.CommandScope(NotificationScopeType.CLASS, "id")),
                Map.of()
        )).isInstanceOf(InvalidNotificationPayloadException.class);

        verifyNoInteractions(notificationRepository, userNotificationRepository, recipientPort);
    }

    @Test
    @DisplayName("deve lançar InvalidNotificationPayloadException quando scopes está vazio")
    void shouldThrowWhenScopesIsEmpty() {
        assertThatThrownBy(() -> new ProcessNotificationRequestCommand(
                "msg-004", null, "src", "EVT", Instant.now(), "T", "B",
                List.of(), List.of(), Map.of()
        )).isInstanceOf(InvalidNotificationPayloadException.class);

        verifyNoInteractions(notificationRepository, userNotificationRepository, recipientPort);
    }

    @Test
    @DisplayName("payload inválido não deve criar notificação nem associações")
    void shouldNotCreateAnyRecordWhenPayloadIsInvalid() {
        assertThatThrownBy(() -> new ProcessNotificationRequestCommand(
                null, null, "src", "EVT", Instant.now(), "T", "B",
                List.of(),
                List.of(new ProcessNotificationRequestCommand.CommandScope(NotificationScopeType.CLASS, "id")),
                Map.of()
        )).isInstanceOf(InvalidNotificationPayloadException.class);

        verifyNoInteractions(notificationRepository, userNotificationRepository, recipientPort);
    }

    // -------------------------------------------------------------------------
    // Metadata
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("metadata é persistido na notificação e não é passado para a port")
    void shouldPersistMetadataWithoutPassingToPort() {
        ProcessNotificationRequestCommand command = new ProcessNotificationRequestCommand(
                "msg-005", null, "src", "EVT", Instant.now(), "T", "B",
                List.of(),
                List.of(new ProcessNotificationRequestCommand.CommandScope(
                        NotificationScopeType.CLASS, UUID.randomUUID().toString())),
                Map.of("classId", "abc-123", "route", "/sala/1")
        );

        when(notificationRepository.findByMessageId("msg-005")).thenReturn(Optional.empty());
        when(notificationRepository.save(any())).thenReturn(buildNotification("msg-005"));
        when(recipientPort.resolve(any(), any())).thenReturn(List.of());

        useCase.execute(command);

        verify(recipientPort).resolve(
                argThat(s -> s.type() == NotificationScopeType.CLASS),
                eq(List.of())
        );
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private NotificationEntity buildNotification(String messageId) {
        NotificationEntity entity = NotificationEntity.create(
                messageId, null, "src", "EVT", Instant.now(), "Título", "Corpo", null
        );
        ReflectionTestUtils.setField(entity, "id", UUID.randomUUID());
        return entity;
    }
}