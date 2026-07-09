package com.portal.conecta.hub.module.notification.application.use_case;

import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand;
import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand.CommandFilter;
import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand.CommandScope;
import com.portal.conecta.hub.module.notification.domain.model.NotificationEntity;
import com.portal.conecta.hub.module.notification.domain.model.NotificationFilterType;
import com.portal.conecta.hub.module.notification.domain.model.NotificationScopeType;
import com.portal.conecta.hub.module.notification.domain.port.NotificationRecipientPort;
import com.portal.conecta.hub.module.notification.domain.port.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessNotificationRequestUseCase")
class ProcessNotificationRequestUseCaseTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationRecipientPort recipientPort;

    @Mock
    private JsonMapper jsonMapper;

    @InjectMocks
    private ProcessNotificationRequestUseCase useCase;

    // ---------------------------------------------------------------
    // helpers
    // ---------------------------------------------------------------

    private ProcessNotificationRequestCommand buildCommand() {
        return buildCommand("msg-001", Map.of());
    }

    private ProcessNotificationRequestCommand buildCommand(String messageId, Map<String, Object> metadata) {
        return new ProcessNotificationRequestCommand(
                messageId,
                "corr-001",
                "SOURCE_APP",
                "EVENT_TYPE",
                Instant.parse("2025-01-01T00:00:00Z"),
                "Título",
                "Corpo da notificação",
                List.of(new CommandFilter(NotificationFilterType.ROLE, "STUDENT")),
                List.of(new CommandScope(NotificationScopeType.USER, UUID.randomUUID().toString())),
                metadata
        );
    }

    private NotificationEntity stubNotification(String messageId) {
        NotificationEntity entity = new NotificationEntity(
                messageId,
                "corr-001",
                "SOURCE_APP",
                "EVENT_TYPE",
                Instant.parse("2025-01-01T00:00:00Z"),
                "Título",
                "Corpo da notificação",
                null
        );
        // simula o ID gerado pelo banco
        return spy(entity);
    }

    // ---------------------------------------------------------------
    // testes
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("quando a notificação ainda não existe")
    class QuandoNaoExiste {

        @Test
        @DisplayName("deve criar e persistir a notificação")
        void deveCriarEPersistirANotificacao() {
            ProcessNotificationRequestCommand command = buildCommand();
            NotificationEntity saved = stubNotification(command.messageId());

            when(notificationRepository.findByMessageId(command.messageId()))
                    .thenReturn(Optional.empty());
            when(notificationRepository.save(any(NotificationEntity.class)))
                    .thenReturn(saved);

            NotificationEntity result = useCase.execute(command);

            assertThat(result).isSameAs(saved);
            verify(notificationRepository).save(any(NotificationEntity.class));
        }

        @Test
        @DisplayName("deve chamar save com os dados corretos do command")
        void deveChamarSaveComDadosCorretos() {
            ProcessNotificationRequestCommand command = buildCommand();
            NotificationEntity saved = stubNotification(command.messageId());

            when(notificationRepository.findByMessageId(command.messageId()))
                    .thenReturn(Optional.empty());
            when(notificationRepository.save(any(NotificationEntity.class)))
                    .thenReturn(saved);

            useCase.execute(command);

            ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
            verify(notificationRepository).save(captor.capture());

            NotificationEntity captured = captor.getValue();
            assertThat(captured.getMessageId()).isEqualTo(command.messageId());
            assertThat(captured.getCorrelationId()).isEqualTo(command.correlationId());
            assertThat(captured.getSource()).isEqualTo(command.source());
            assertThat(captured.getEventType()).isEqualTo(command.eventType());
            assertThat(captured.getOccurredAt()).isEqualTo(command.occurredAt());
            assertThat(captured.getTitle()).isEqualTo(command.title());
            assertThat(captured.getBody()).isEqualTo(command.body());
        }
    }

    @Nested
    @DisplayName("quando a notificação já existe (messageId duplicado)")
    class QuandoJaExiste {

        @Test
        @DisplayName("deve retornar a notificação existente sem chamar save")
        void deveRetornarExistenteEIgnorarSave() {
            ProcessNotificationRequestCommand command = buildCommand();
            NotificationEntity existing = stubNotification(command.messageId());

            when(notificationRepository.findByMessageId(command.messageId()))
                    .thenReturn(Optional.of(existing));

            NotificationEntity result = useCase.execute(command);

            assertThat(result).isSameAs(existing);
            verify(notificationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("dispatch para o recipientPort")
    class Dispatch {

        @Test
        @DisplayName("deve chamar dispatch com a notificação, scopes e filters do command")
        void deveChamarDispatchComDadosCorretos() {
            ProcessNotificationRequestCommand command = buildCommand();
            NotificationEntity saved = stubNotification(command.messageId());

            when(notificationRepository.findByMessageId(command.messageId()))
                    .thenReturn(Optional.empty());
            when(notificationRepository.save(any())).thenReturn(saved);

            useCase.execute(command);

            verify(recipientPort).dispatch(saved, command.scopes(), command.filters());
        }

        @Test
        @DisplayName("deve chamar dispatch mesmo quando a notificação já existia")
        void deveChamarDispatchQuandoNotificacaoJaExistia() {
            ProcessNotificationRequestCommand command = buildCommand();
            NotificationEntity existing = stubNotification(command.messageId());

            when(notificationRepository.findByMessageId(command.messageId()))
                    .thenReturn(Optional.of(existing));

            useCase.execute(command);

            verify(recipientPort).dispatch(existing, command.scopes(), command.filters());
        }

        @Test
        @DisplayName("deve chamar dispatch exatamente uma vez por execução")
        void deveChamarDispatchUmaVez() {
            ProcessNotificationRequestCommand command = buildCommand();
            NotificationEntity saved = stubNotification(command.messageId());

            when(notificationRepository.findByMessageId(command.messageId()))
                    .thenReturn(Optional.empty());
            when(notificationRepository.save(any())).thenReturn(saved);

            useCase.execute(command);

            verify(recipientPort, times(1)).dispatch(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("serialização de metadata")
    class Metadata {

        @Test
        @DisplayName("deve passar metadata nula quando o map está vazio")
        void devePassarMetadataNulaQuandoMapVazio() {
            ProcessNotificationRequestCommand command = buildCommand("msg-002", Map.of());
            NotificationEntity saved = stubNotification(command.messageId());

            when(notificationRepository.findByMessageId(command.messageId()))
                    .thenReturn(Optional.empty());
            when(notificationRepository.save(any())).thenReturn(saved);

            useCase.execute(command);

            ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
            verify(notificationRepository).save(captor.capture());

            assertThat(captor.getValue().getMetadata()).isNull();
        }

        @Test
        @DisplayName("deve passar metadata nula quando o map é null")
        void devePassarMetadataNulaQuandoMapNull() {
            ProcessNotificationRequestCommand command = buildCommand("msg-003", null);
            NotificationEntity saved = stubNotification(command.messageId());

            when(notificationRepository.findByMessageId(command.messageId()))
                    .thenReturn(Optional.empty());
            when(notificationRepository.save(any())).thenReturn(saved);

            useCase.execute(command);

            ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
            verify(notificationRepository).save(captor.capture());

            assertThat(captor.getValue().getMetadata()).isNull();
        }

        @Test
        @DisplayName("deve serializar metadata para JsonNode quando o map tem dados")
        void deveSerializarMetadataQuandoMapTemDados() {
            Map<String, Object> meta = Map.of("chave", "valor");
            ProcessNotificationRequestCommand command = buildCommand("msg-004", meta);
            NotificationEntity saved = stubNotification(command.messageId());

            when(notificationRepository.findByMessageId(command.messageId()))
                    .thenReturn(Optional.empty());
            when(notificationRepository.save(any())).thenReturn(saved);
            when(jsonMapper.valueToTree(meta))
                    .thenReturn(new JsonMapper().valueToTree(meta));

            useCase.execute(command);

            ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
            verify(notificationRepository).save(captor.capture());

            assertThat(captor.getValue().getMetadata()).isNotNull();
            assertThat(captor.getValue().getMetadata().get("chave").asText()).isEqualTo("valor");
        }
    }

    @Nested
    @DisplayName("ordem das operações")
    class OrdemDasOperacoes {

        @Test
        @DisplayName("deve salvar antes de chamar dispatch")
        void deveSalvarAntesDeDispatch() {
            ProcessNotificationRequestCommand command = buildCommand();
            NotificationEntity saved = stubNotification(command.messageId());

            when(notificationRepository.findByMessageId(command.messageId()))
                    .thenReturn(Optional.empty());
            when(notificationRepository.save(any())).thenReturn(saved);

            var order = inOrder(notificationRepository, recipientPort);

            useCase.execute(command);

            order.verify(notificationRepository).save(any());
            order.verify(recipientPort).dispatch(any(), any(), any());
        }
    }
}