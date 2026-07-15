package com.portal.conecta.hub.module.auth.application.use_case;

import com.portal.conecta.hub.module.auth.application.command.ActivateAccountCommand;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.AccountActivationToken;
import com.portal.conecta.hub.module.user.domain.model.AccountStatus;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.AccountActivationTokenPort;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivateAccountUseCaseTest {

    @Mock
    private AccountActivationTokenPort activationTokenPort;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private ActivateAccountUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ActivateAccountUseCase(activationTokenPort, userRepository, passwordEncoder);
    }

    @Test
    void activatesUserWithValidToken() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.createPendingActivation(
                "Student",
                "student@estudante.sesisenai.org.br",
                "unusable-hash",
                TypeUser.STUDENT,
                null
        );
        ReflectionTestUtils.setField(user, "id", userId);

        when(activationTokenPort.findByRawToken("token"))
                .thenReturn(Optional.of(new AccountActivationToken(userId, Instant.now().plusSeconds(60), null)));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");

        useCase.execute(new ActivateAccountCommand("token", "123456"));

        assertTrue(user.isActive());
        assertEquals("encoded-password", user.getPasswordHash());
        verify(userRepository).save(user);
        verify(activationTokenPort).markAsUsed("token");
    }

    @Test
    void rejectsExpiredToken() {
        UUID userId = UUID.randomUUID();
        when(activationTokenPort.findByRawToken("token"))
                .thenReturn(Optional.of(new AccountActivationToken(userId, Instant.now().minusSeconds(60), null)));

        assertThrows(InvalidUserDataException.class,
                () -> useCase.execute(new ActivateAccountCommand("token", "123456")));

        verify(userRepository, never()).save(any(UserEntity.class));
        verify(activationTokenPort, never()).markAsUsed("token");
    }

    @Test
    void rejectsUsedToken() {
        UUID userId = UUID.randomUUID();
        when(activationTokenPort.findByRawToken("token"))
                .thenReturn(Optional.of(new AccountActivationToken(userId, Instant.now().plusSeconds(60), Instant.now())));

        assertThrows(InvalidUserDataException.class,
                () -> useCase.execute(new ActivateAccountCommand("token", "123456")));

        verify(userRepository, never()).save(any(UserEntity.class));
        verify(activationTokenPort, never()).markAsUsed("token");
    }

    @Test
    void rejectsUnknownToken() {
        when(activationTokenPort.findByRawToken("token")).thenReturn(Optional.empty());

        assertThrows(InvalidUserDataException.class,
                () -> useCase.execute(new ActivateAccountCommand("token", "123456")));

        verify(userRepository, never()).save(any(UserEntity.class));
        verify(activationTokenPort, never()).markAsUsed("token");
    }

    @Test
    void rejectsRemovedUser() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.createPendingActivation(
                "Student",
                "student@estudante.sesisenai.org.br",
                "unusable-hash",
                TypeUser.STUDENT,
                null
        );
        user.delete(null);

        when(activationTokenPort.findByRawToken("token"))
                .thenReturn(Optional.of(new AccountActivationToken(userId, Instant.now().plusSeconds(60), null)));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(UserNotFoundException.class,
                () -> useCase.execute(new ActivateAccountCommand("token", "123456")));

        verify(userRepository, never()).save(any(UserEntity.class));
        verify(activationTokenPort, never()).markAsUsed("token");
    }

    @Test
    void rejectsAlreadyActiveUser() {
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.createPendingActivation(
                "Student",
                "student@estudante.sesisenai.org.br",
                "unusable-hash",
                TypeUser.STUDENT,
                null
        );
        ReflectionTestUtils.setField(user, "active", true);
        ReflectionTestUtils.setField(user, "accountStatus", AccountStatus.ACTIVE);

        when(activationTokenPort.findByRawToken("token"))
                .thenReturn(Optional.of(new AccountActivationToken(userId, Instant.now().plusSeconds(60), null)));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        InvalidUserDataException exception = assertThrows(InvalidUserDataException.class,
                () -> useCase.execute(new ActivateAccountCommand("token", "123456")));

        assertTrue(exception.getMessage().contains("ativada anteriormente"));
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(activationTokenPort, never()).markAsUsed("token");
    }
}
