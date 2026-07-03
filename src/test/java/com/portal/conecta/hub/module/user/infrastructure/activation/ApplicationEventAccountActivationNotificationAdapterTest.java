package com.portal.conecta.hub.module.user.infrastructure.activation;

import com.portal.conecta.hub.module.user.application.event.UserCreatedEvent;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ApplicationEventAccountActivationNotificationAdapterTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ApplicationEventAccountActivationNotificationAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ApplicationEventAccountActivationNotificationAdapter(eventPublisher);
    }

    @Test
    void devePublicarEventoComDadosDoUsuario() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.createPendingActivation(
                "Maria Silva", "maria@example.com", "unusable-hash", TypeUser.STUDENT, null
        );
        ReflectionTestUtils.setField(user, "id", userId);

        String rawToken = "raw-token-123";
        Instant expiresAt = Instant.parse("2026-07-03T10:00:00Z");

        adapter.requestActivation(user, rawToken, expiresAt);

        ArgumentCaptor<UserCreatedEvent> captor = ArgumentCaptor.forClass(UserCreatedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        UserCreatedEvent publicado = captor.getValue();
        assertThat(publicado.userId()).isEqualTo(userId);
        assertThat(publicado.name()).isEqualTo("Maria Silva");
        assertThat(publicado.email()).isEqualTo("maria@example.com");
        assertThat(publicado.rawActivationToken()).isEqualTo(rawToken);
        assertThat(publicado.expiresAt()).isEqualTo(expiresAt);
    }
}