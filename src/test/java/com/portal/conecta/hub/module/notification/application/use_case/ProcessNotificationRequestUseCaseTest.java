package com.portal.conecta.hub.module.notification.application.use_case;

import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand;
import com.portal.conecta.hub.module.notification.domain.exception.InvalidNotificationPayloadException;
import com.portal.conecta.hub.module.notification.domain.model.NotificationEntity;
import com.portal.conecta.hub.module.notification.domain.model.NotificationScopeType;
import com.portal.conecta.hub.module.notification.domain.port.NotificationRecipientPort;
import com.portal.conecta.hub.module.notification.domain.port.NotificationRepository;
import com.portal.conecta.hub.module.notification.domain.port.UserNotificationRepository;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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

    @InjectMocks
    private ProcessNotificationRequestUseCase useCase;

    private ProcessNotificationRequestCommand validCommand;
    private UserEntity userA;
    private UserEntity userB;

    @BeforeEach
    void setUp() {
        userA = new UserEntity("Aluno A", "alunoa@estudante.sesisenai.org.br", "hash", TypeUser.STUDENT);
        ReflectionTestUtils.setField(userA, "id", UUID.randomUUID());

        userB = new UserEntity("Aluno B", "alunob@estudante.sesisenai.org.br", "hash", TypeUser.STUDENT);
        ReflectionTestUtils.setField(userB, "id", UUID.randomUUID());

        validCommand = new ProcessNotificationRequestCommand(
                "msg-001",
                "corr-001",
                "seat_map",
                "SEAT_UPDATED",
                Instant.now(),
                "Mapa atualizado",
                "O mapa de sala foi atualizado.",
                List.of(),
                List.of(new ProcessNotificationRequestCommand.CommandScope(NotificationScopeType.CLASS, UUID.randomUUID().toString())),
                Map.of()
        );
    }

    // -------------------------------------------------------------------------
    // Sucesso — criação de notificação base
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deve criar notificação base quando payload é válido")
    void shouldCreateNotificationWhenPayloadIsValid() {
        NotificationEntity saved = buildNotification("msg-001");

        when(notificationRepository.findByMessageId("msg-001")).thenReturn(Optional.empty());
        when(notificationRepository.save(any())).thenReturn(saved);
        when(recipientPort.resolve(any(), any())).thenReturn(List.of());

        NotificationEntity result = useCase.execute(validCommand);

        assertThat(result.getMessageId()).isEqualTo("msg-001");
        verify(notificationRepository).save(any(NotificationEntity.class));
    }

    @Test
    @DisplayName("deve chamar a port de resolução de destinatários para cada scope")
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

        NotificationEntity saved = buildNotification("msg-002");
        when(notificationRepository.findByMessageId("msg-002")).thenReturn(Optional.empty());
        when(notificationRepository.save(any())).thenReturn(saved);
        when(recipientPort.resolve(any(), any())).thenReturn(List.of());

        useCase.execute(command);

        verify(recipientPort, times(2)).resolve(any(), any());
    }

    @Test
    @DisplayName("deve associar usuários retornados pela port à notificação")
    void shouldAssociateResolvedUsersToNotification() {
        NotificationEntity saved = buildNotification("msg-001");

        when(notificationRepository.findByMessageId("msg-001")).thenReturn(Optional.empty());
        when(notificationRepository.save(any())).thenReturn(saved);
        when(recipientPort.resolve(any(), any())).thenReturn(List.of(userA, userB));
        when(userNotificationRepository.existsByNotificationIdAndUserId(any(), any())).thenReturn(false);
        when(userNotificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        useCase.execute(validCommand);

        verify(userNotificationRepository, times(2)).save(any());
    }

    // -------------------------------------------------------------------------
    // Idempotência
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("messageId duplicado não deve criar nova notificação base")
    void shouldReuseExistingNotificationWhenMessageIdIsDuplicate() {
        NotificationEntity existing = buildNotification("msg-001");

        when(notificationRepository.findByMessageId("msg-001")).thenReturn(Optional.of(existing));
        when(recipientPort.resolve(any(), any())).thenReturn(List.of());

        useCase.execute(validCommand);

        verify(notificationRepository, never()).save(any(NotificationEntity.class));
    }

    @Test
    @DisplayName("usuário duplicado no retorno da port não deve gerar associação duplicada")
    void shouldNotDuplicateUserNotificationWhenUserAppearsInMultipleScopes() {
        ProcessNotificationRequestCommand command = new ProcessNotificationRequestCommand(
                "msg-003", null, "src", "EVT", Instant.now(), "T", "B",
                List.of(),
                List.of(
                        new ProcessNotificationRequestCommand.CommandScope(NotificationScopeType.CLASS, UUID.randomUUID().toString()),
                        new ProcessNotificationRequestCommand.CommandScope(NotificationScopeType.COURSE, UUID.randomUUID().toString())
                ),
                Map.of()
        );

        NotificationEntity saved = buildNotification("msg-003");
        when(notificationRepository.findByMessageId("msg-003")).thenReturn(Optional.empty());
        when(notificationRepository.save(any())).thenReturn(saved);
        // mesmo userA retornado pelos dois scopes
        when(recipientPort.resolve(any(), any())).thenReturn(List.of(userA));
        when(userNotificationRepository.existsByNotificationIdAndUserId(any(), eq(userA.getId()))).thenReturn(false);
        when(userNotificationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        useCase.execute(command);

        // userA só deve ser associado uma vez, mesmo aparecendo em 2 scopes
        verify(userNotificationRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("não deve criar associação se usuário já está associado no banco")
    void shouldNotDuplicateWhenAssociationAlreadyExistsInDatabase() {
        NotificationEntity existing = buildNotification("msg-001");

        when(notificationRepository.findByMessageId("msg-001")).thenReturn(Optional.of(existing));
        when(recipientPort.resolve(any(), any())).thenReturn(List.of(userA));
        when(userNotificationRepository.existsByNotificationIdAndUserId(any(), any())).thenReturn(true);

        useCase.execute(validCommand);

        verify(userNotificationRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // Validação de payload
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deve lançar InvalidNotificationPayloadException quando messageId está ausente")
    void shouldThrowWhenMessageIdIsBlank() {
        ProcessNotificationRequestCommand command = new ProcessNotificationRequestCommand(
                "", null, "src", "EVT", Instant.now(), "T", "B",
                List.of(),
                List.of(new ProcessNotificationRequestCommand.CommandScope(NotificationScopeType.CLASS, "id")),
                Map.of()
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidNotificationPayloadException.class);

        verifyNoInteractions(notificationRepository, userNotificationRepository, recipientPort);
    }

    @Test
    @DisplayName("deve lançar InvalidNotificationPayloadException quando scopes está vazio")
    void shouldThrowWhenScopesIsEmpty() {
        ProcessNotificationRequestCommand command = new ProcessNotificationRequestCommand(
                "msg-004", null, "src", "EVT", Instant.now(), "T", "B",
                List.of(), List.of(), Map.of()
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidNotificationPayloadException.class);

        verifyNoInteractions(notificationRepository, userNotificationRepository, recipientPort);
    }

    @Test
    @DisplayName("payload inválido não deve criar notificação nem associações")
    void shouldNotCreateAnyRecordWhenPayloadIsInvalid() {
        ProcessNotificationRequestCommand command = new ProcessNotificationRequestCommand(
                null, null, "src", "EVT", Instant.now(), "T", "B",
                List.of(),
                List.of(new ProcessNotificationRequestCommand.CommandScope(NotificationScopeType.CLASS, "id")),
                Map.of()
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidNotificationPayloadException.class);

        verifyNoInteractions(notificationRepository, userNotificationRepository, recipientPort);
    }

    // -------------------------------------------------------------------------
    // Metadata
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("metadata é persistido na notificação e não interfere nos destinatários")
    void shouldPersistMetadataWithoutAffectingRecipients() {
        ProcessNotificationRequestCommand command = new ProcessNotificationRequestCommand(
                "msg-005", null, "src", "EVT", Instant.now(), "T", "B",
                List.of(),
                List.of(new ProcessNotificationRequestCommand.CommandScope(NotificationScopeType.CLASS, UUID.randomUUID().toString())),
                Map.of("classId", "abc-123", "route", "/sala/1")
        );

        NotificationEntity saved = buildNotification("msg-005");
        when(notificationRepository.findByMessageId("msg-005")).thenReturn(Optional.empty());
        when(notificationRepository.save(any())).thenReturn(saved);
        when(recipientPort.resolve(any(), any())).thenReturn(List.of());

        useCase.execute(command);

        // port é chamada com scope e filters — metadata não entra
        verify(recipientPort).resolve(
                argThat(s -> s.type() == NotificationScopeType.CLASS),
                eq(List.of())
        );
        verify(notificationRepository).save(any());
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