package com.portal.conecta.hub.module.notification.infrastructure.messaging.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;

import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand;
import com.portal.conecta.hub.module.notification.application.usecase.ProcessNotificationRequestUseCase;
import com.portal.conecta.hub.module.notification.infrastructure.messaging.dto.NotificationRecipient;
import com.portal.conecta.hub.module.notification.infrastructure.messaging.dto.NotificationRecipientFilters;
import com.portal.conecta.hub.module.notification.infrastructure.messaging.dto.NotificationRecipientScope;
import com.portal.conecta.hub.module.notification.infrastructure.messaging.dto.NotificationRequestPayload;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class NotificationMessageConsumerTest {

    @Mock
    private ProcessNotificationRequestUseCase useCase;

    @InjectMocks
    private NotificationMessageConsumer consumer;

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        }
    }

    @Test
    void shouldProcessValidNotificationRequestSuccessfully() {
        NotificationRequestPayload payload = createValidPayload();
        ArgumentCaptor<ProcessNotificationRequestCommand> commandCaptor =
                ArgumentCaptor.forClass(ProcessNotificationRequestCommand.class);

        consumer.consume(payload);

        verify(useCase).execute(commandCaptor.capture());
        ProcessNotificationRequestCommand capturedCommand = commandCaptor.getValue();

        assertThat(capturedCommand.messageId()).isEqualTo(payload.messageId());
        assertThat(capturedCommand.correlationId()).isEqualTo(payload.correlationId());
        assertThat(capturedCommand.source()).isEqualTo(payload.source());
        assertThat(capturedCommand.eventType()).isEqualTo(payload.eventType());
        assertThat(capturedCommand.title()).isEqualTo(payload.title());
        assertThat(capturedCommand.body()).isEqualTo(payload.body());
        assertThat(capturedCommand.occurredAt()).isEqualTo(payload.occurredAt());
        assertThat(capturedCommand.metadata()).containsEntry("key", "value");

        assertThat(capturedCommand.recipients()).hasSize(1);
        var capturedRecipient = capturedCommand.recipients().get(0);
        assertThat(capturedRecipient.scope().type()).isEqualTo("TURMA");
        assertThat(capturedRecipient.scope().id()).isEqualTo("12345");
        assertThat(capturedRecipient.filters().userTypes()).containsExactly("ALUNO");
    }

    @Test
    void shouldPropagateExceptionWhenUseCaseFailsAllowingRabbitMqRetry() {
        NotificationRequestPayload payload = createValidPayload();
        doThrow(new RuntimeException("Erro transitório de banco fora do ar"))
                .when(useCase).execute(any(ProcessNotificationRequestCommand.class));

        assertThatThrownBy(() -> consumer.consume(payload))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Erro transitório de banco fora do ar");
    }

    @Test
    void shouldFailValidationWhenRequiredFieldsAreMissingOrBlank() {
        // Arrange
        NotificationRequestPayload invalidPayload = new NotificationRequestPayload(
                "",
                null,
                "  ",
                "",
                null,
                "",
                " ",
                Collections.emptyList(),
                Map.of()
        );

        Set<ConstraintViolation<NotificationRequestPayload>> violations = validator.validate(invalidPayload);

        assertThat(violations).hasSize(8);

        List<String> errorMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList();

        assertThat(errorMessages).contains(
                "O ID da mensagem é obrigatório.",
                "O correlationId é obrigatório.",
                "A origem (source) é obrigatória.",
                "O tipo de evento (eventType) é obrigatório.",
                "A data de ocorrência (occurredAt) é obrigatória.",
                "O título é obrigatório.",
                "O corpo da mensagem (body) é obrigatório.",
                "A lista de destinatários não pode ser vazia."
        );
    }

    @Test
    void shouldFailValidationWhenRecipientScopeIsInvalid() {
        // Arrange
        NotificationRecipientScope invalidScope = new NotificationRecipientScope("", null);
        NotificationRecipient recipient = new NotificationRecipient(invalidScope, null);

        Set<ConstraintViolation<NotificationRecipient>> violations = validator.validate(recipient);

        List<String> errorMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList();

        assertThat(errorMessages).contains(
                "O tipo do escopo (type) é obrigatório.",
                "O ID do escopo é obrigatório."
        );
    }

    private NotificationRequestPayload createValidPayload() {
        NotificationRecipientScope scope = new NotificationRecipientScope("TURMA", "12345");
        NotificationRecipientFilters filters = new NotificationRecipientFilters(List.of("ALUNO"), List.of("USER"));
        NotificationRecipient recipient = new NotificationRecipient(scope, filters);

        return new NotificationRequestPayload(
                "msg-uuid-123",
                "corr-uuid-456",
                "portal-conecta",
                "notification.requested",
                OffsetDateTime.now(),
                "Nova Atividade Disponível",
                "Você possui uma nova tarefa para entregar.",
                List.of(recipient),
                Map.of("key", "value")
        );
    }
}