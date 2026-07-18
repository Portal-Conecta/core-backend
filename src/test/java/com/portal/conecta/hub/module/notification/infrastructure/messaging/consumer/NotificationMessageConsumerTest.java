package com.portal.conecta.hub.module.notification.infrastructure.messaging.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.portal.conecta.hub.module.notification.application.command.ProcessNotificationRequestCommand;
import com.portal.conecta.hub.module.notification.application.use_case.ProcessNotificationRequestUseCase;
import com.portal.conecta.hub.module.notification.infrastructure.messaging.dto.filter.NotificationFilterPayload;
import com.portal.conecta.hub.module.notification.infrastructure.messaging.dto.NotificationMessagePayload;
import com.portal.conecta.hub.module.notification.infrastructure.messaging.dto.filter.NotificationFilterType;
import com.portal.conecta.hub.module.notification.infrastructure.messaging.dto.scope.NotificationScopePayload;
import com.portal.conecta.hub.module.notification.infrastructure.messaging.dto.scope.NotificationScopeType;
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

import java.time.Instant;
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
    void shouldProcessValidNotificationMessageSuccessfully() {
        NotificationMessagePayload payload = createValidPayload();
        ArgumentCaptor<ProcessNotificationRequestCommand> commandCaptor =
                ArgumentCaptor.forClass(ProcessNotificationRequestCommand.class);

        consumer.consume(payload);

        verify(useCase).execute(commandCaptor.capture());
        ProcessNotificationRequestCommand capturedCommand = commandCaptor.getValue();

        assertThat(capturedCommand.messageId()).isEqualTo(payload.messageId());
        assertThat(capturedCommand.correlationId()).isEqualTo(payload.correlationId());
        assertThat(capturedCommand.source()).isEqualTo("seatmap-service");
        assertThat(capturedCommand.eventType()).isEqualTo("SEAT_MAP_UPDATED");
        assertThat(capturedCommand.title()).isEqualTo("Mapa atualizado");

        assertThat(capturedCommand.filters()).hasSize(1);
        assertThat(capturedCommand.filters().get(0).type()).isEqualTo(NotificationFilterType.ROLE);
        assertThat(capturedCommand.filters().get(0).value()).isEqualTo("STUDENT");

        assertThat(capturedCommand.scopes()).hasSize(3);
        assertThat(capturedCommand.scopes().get(0).type()).isEqualTo(NotificationScopeType.CLASS);
        assertThat(capturedCommand.scopes().get(0).correlationId()).isEqualTo("class-101");

        assertThat(capturedCommand.scopes().get(1).type()).isEqualTo(NotificationScopeType.USER);
        assertThat(capturedCommand.scopes().get(1).correlationId()).isEqualTo("user-id");

        assertThat(capturedCommand.scopes().get(2).type()).isEqualTo(NotificationScopeType.COURSE);
        assertThat(capturedCommand.scopes().get(2).correlationId()).isEqualTo("course-ds");
    }

    @Test
    void shouldFailValidationWhenRequiredFieldsAreMissingOrBlank() {
        NotificationMessagePayload invalidPayload = new NotificationMessagePayload(
                "",
                null,
                "  ",
                "",
                null,
                "",
                " ",
                null,
                Collections.emptyList(),
                Map.of()
        );

        Set<ConstraintViolation<NotificationMessagePayload>> violations = validator.validate(invalidPayload);

        assertThat(violations).hasSize(7);

        List<String> errorMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList();

        assertThat(errorMessages).contains(
                "O ID da mensagem é obrigatório.",
                "A origem (source) é obrigatória.",
                "O tipo de evento (eventType) é obrigatório.",
                "A data de ocorrência (occurredAt) é obrigatória.",
                "O título é obrigatório.",
                "O corpo da mensagem (body) é obrigatório.",
                "Pelo menos um escopo deve ser informado."
        );
    }

    private NotificationMessagePayload createValidPayload() {
        return new NotificationMessagePayload(
                "msg-01JY2Q4ZK7F4T2Z1X9X3H8R6QP",
                "corr-01JY2Q4ZK7F4T2Z1X9X3H8R6QP",
                "seatmap-service",
                "SEAT_MAP_UPDATED",
                Instant.parse("2026-06-17T20:55:00Z"),
                "Mapa atualizado",
                "A turma foi reorganizada.",
                List.of(new NotificationFilterPayload(NotificationFilterType.ROLE, "STUDENT")),
                List.of(
                        new NotificationScopePayload(NotificationScopeType.CLASS, "class-101"),
                        new NotificationScopePayload(NotificationScopeType.USER, "user-id"),
                        new NotificationScopePayload(NotificationScopeType.COURSE, "course-ds")
                ),
                Map.of("classId", "class-101", "route", "/turmas/class-101/mapa")
        );
    }

    @Test
    void shouldProcessNotificationMessageWithShiftFilter() {
        NotificationMessagePayload payload = createValidPayloadWithRoleAndShiftFilters();
        ArgumentCaptor<ProcessNotificationRequestCommand> commandCaptor =
                ArgumentCaptor.forClass(ProcessNotificationRequestCommand.class);

        consumer.consume(payload);

        verify(useCase).execute(commandCaptor.capture());
        ProcessNotificationRequestCommand capturedCommand = commandCaptor.getValue();

        assertThat(capturedCommand.filters()).hasSize(2);

        assertThat(capturedCommand.filters().get(0).type()).isEqualTo(NotificationFilterType.ROLE);
        assertThat(capturedCommand.filters().get(0).value()).isEqualTo("STUDENT");

        assertThat(capturedCommand.filters().get(1).type()).isEqualTo(NotificationFilterType.SHIFT);
        assertThat(capturedCommand.filters().get(1).value()).isEqualTo("FULL_AM_PM");
    }

    private NotificationMessagePayload createValidPayloadWithRoleAndShiftFilters() {
        return new NotificationMessagePayload(
                "msg-01JY2Q4ZK7F4T2Z1X9X3H8R6QP",
                "corr-01JY2Q4ZK7F4T2Z1X9X3H8R6QP",
                "seatmap-service",
                "SEAT_MAP_UPDATED",
                Instant.parse("2026-06-17T20:55:00Z"),
                "Mapa atualizado",
                "A turma foi reorganizada.",
                List.of(
                        new NotificationFilterPayload(NotificationFilterType.ROLE, "STUDENT"),
                        new NotificationFilterPayload(NotificationFilterType.SHIFT, "FULL_AM_PM")
                ),
                List.of(
                        new NotificationScopePayload(NotificationScopeType.CLASS, "class-101"),
                        new NotificationScopePayload(NotificationScopeType.USER, "user-id"),
                        new NotificationScopePayload(NotificationScopeType.COURSE, "course-ds")
                ),
                Map.of("classId", "class-101", "route", "/turmas/class-101/mapa")
        );
    }
}