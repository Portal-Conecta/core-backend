package com.portal.conecta.hub.module.user.infrastructure.activation;

import com.portal.conecta.hub.module.user.application.event.UserCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserCreatedEventListenerTest {

    @Mock
    private AccountActivationEmailService emailService;

    private UserCreatedEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new UserCreatedEventListener(emailService);
    }

    @Test
    void deveChamarServicoDeEmailComDadosDoEvento() {
        UserCreatedEvent event = new UserCreatedEvent(
                UUID.randomUUID(), "Maria Silva", "maria@example.com", "raw-token", Instant.now()
        );

        listener.handle(event);

        verify(emailService).sendActivationEmail("Maria Silva", "maria@example.com", "raw-token", event.expiresAt());
    }

    @Test
    void naoDevePropagarExcecaoQuandoServicoDeEmailFalha() {
        UserCreatedEvent event = new UserCreatedEvent(
                UUID.randomUUID(), "Maria Silva", "maria@example.com", "raw-token", Instant.now()
        );

        doThrow(new IllegalStateException("Falha SMTP"))
                .when(emailService)
                .sendActivationEmail("Maria Silva", "maria@example.com", "raw-token", event.expiresAt());

        assertThatCode(() -> listener.handle(event)).doesNotThrowAnyException();
    }
}