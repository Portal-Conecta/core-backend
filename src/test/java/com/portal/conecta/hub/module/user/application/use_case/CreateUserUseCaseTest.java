package com.portal.conecta.hub.module.user.application.use_case;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.portal.conecta.hub.module.user.application.command.CreateUserCommand;
import com.portal.conecta.hub.module.user.domain.exception.EmailAlreadyInUseException;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.AccountActivationNotificationPort;
import com.portal.conecta.hub.module.user.domain.port.AccountActivationTokenPort;
import com.portal.conecta.hub.module.user.domain.port.AccountActivationTokenTtlPort;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.module.user.domain.policy.UserEmailPolicy;
import com.portal.conecta.hub.module.user.domain.validator.UserPermissionValidator;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RequestContextProvider contextProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccountActivationTokenPort activationTokenPort;

    @Mock
    private AccountActivationNotificationPort activationNotificationPort;

    @Mock
    private AccountActivationTokenTtlPort activationTokenTtlPort;

    private CreateUserUseCase useCase;

    @BeforeEach
    void setUp() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger("com.portal.conecta.hub.module.user.application.use_case").setLevel(Level.INFO);

        when(activationTokenTtlPort.activationTokenTtl()).thenReturn(Duration.ofHours(24));

        useCase = new CreateUserUseCase(
                userRepository,
                new UserEmailPolicy(),
                new UserPermissionValidator(),
                contextProvider,
                passwordEncoder,
                activationTokenPort,
                activationNotificationPort,
                activationTokenTtlPort
        );
    }

    @Test
    void createsPendingUserAndRequestsActivation() {
        UUID adminId = UUID.randomUUID();
        UserEntity creator = new UserEntity("Admin", "admin@portal.test", "admin-hash", TypeUser.ADMIN);

        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(adminId, TypeUser.ADMIN, List.of()));
        when(userRepository.existsByEmailIgnoreCase("student@estudante.sesisenai.org.br")).thenReturn(false);
        when(userRepository.findById(adminId)).thenReturn(Optional.of(creator));
        when(passwordEncoder.encode(anyString())).thenReturn("unusable-hash");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(activationTokenPort.createToken(any(UserEntity.class), any(Instant.class))).thenReturn("activation-token");

        UserEntity result = useCase.execute(new CreateUserCommand(
                "  Student One  ",
                "STUDENT@ESTUDANTE.SESISENAI.ORG.BR ",
                TypeUser.STUDENT
        ));

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());

        UserEntity savedUser = userCaptor.getValue();
        assertEquals("Student One", savedUser.getName());
        assertEquals("student@estudante.sesisenai.org.br", savedUser.getEmail());
        assertEquals("unusable-hash", savedUser.getPasswordHash());
        assertEquals(TypeUser.STUDENT, savedUser.getTypeUser());
        assertEquals(creator, savedUser.getCreatedBy());
        assertFalse(savedUser.isActive());
        assertNull(savedUser.getDeletedAt());

        verify(activationTokenPort).createToken(any(UserEntity.class), any(Instant.class));
        verify(activationNotificationPort).requestActivation(eq(savedUser), eq("activation-token"), any(Instant.class));

        assertEquals(savedUser.getId(), result.getId());
        assertEquals("Student One", result.getName());
        assertEquals("student@estudante.sesisenai.org.br", result.getEmail());
        assertEquals(TypeUser.STUDENT, result.getTypeUser());
        assertFalse(result.isActive());
        assertNull(result.getDeletedAt());
    }

    @Test
    void rejectsDuplicatedEmailBeforeEncodingOrSaving() {
        UUID adminId = UUID.randomUUID();

        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(adminId, TypeUser.ADMIN, List.of()));
        when(userRepository.existsByEmailIgnoreCase("duplicate@estudante.sesisenai.org.br")).thenReturn(true);

        assertThrows(EmailAlreadyInUseException.class, () -> useCase.execute(new CreateUserCommand(
                "Duplicate",
                "duplicate@estudante.sesisenai.org.br",
                TypeUser.STUDENT
        )));

        verify(userRepository, never()).findById(any(UUID.class));
        verify(userRepository, never()).save(any(UserEntity.class));
        verifyNoInteractions(activationTokenPort, activationNotificationPort);
    }

    @Test
    void rejectsInvalidNameBeforeEncodingOrSaving() {
        UUID adminId = UUID.randomUUID();
        UserEntity creator = new UserEntity("Admin", "admin@portal.test", "admin-hash", TypeUser.ADMIN);

        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(adminId, TypeUser.ADMIN, List.of()));
        when(userRepository.existsByEmailIgnoreCase("student@estudante.sesisenai.org.br")).thenReturn(false);
        when(userRepository.findById(adminId)).thenReturn(Optional.of(creator));

        assertThrows(InvalidUserDataException.class, () -> useCase.execute(new CreateUserCommand(
                "   ",
                "student@estudante.sesisenai.org.br",
                TypeUser.STUDENT
        )));

        verify(userRepository, never()).save(any(UserEntity.class));
        verifyNoInteractions(activationTokenPort, activationNotificationPort);
    }

    @Test
    void rejectsUserWithoutPermissionBeforeCheckingEmail() {
        UUID wegId = UUID.randomUUID();

        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(wegId, TypeUser.WEG, List.of()));

        assertThrows(UserPermissionDeniedException.class, () -> useCase.execute(new CreateUserCommand(
                "Teacher",
                "teacher@estudante.sesisenai.org.br",
                TypeUser.TEACHER
        )));

        verifyNoInteractions(userRepository, passwordEncoder, activationTokenPort, activationNotificationPort);
    }

    @Test
    void rejectsEmailOutsideSenaiOrWegDomainsBeforeEncodingOrSaving() {
        UUID adminId = UUID.randomUUID();
        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(adminId, TypeUser.ADMIN, List.of()));

        assertThrows(InvalidUserDataException.class, () -> useCase.execute(new CreateUserCommand(
                "External",
                "external@example.com",
                TypeUser.STUDENT
        )));

        verify(userRepository, never()).existsByEmailIgnoreCase(anyString());
        verify(userRepository, never()).findById(any(UUID.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
        verifyNoInteractions(activationTokenPort, activationNotificationPort);
    }

    @Test
    @DisplayName("deve lancar EmailAlreadyInUseException sem log de negocio quando e-mail ja esta em uso")
    void shouldThrowWithoutBusinessLogWhenEmailAlreadyInUse(CapturedOutput output) {
        UUID adminId = UUID.randomUUID();

        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(adminId, TypeUser.ADMIN, List.of()));
        when(userRepository.existsByEmailIgnoreCase("duplicate@estudante.sesisenai.org.br")).thenReturn(true);

        assertThrows(EmailAlreadyInUseException.class, () -> useCase.execute(new CreateUserCommand(
                "Duplicate",
                "duplicate@estudante.sesisenai.org.br",
                TypeUser.STUDENT
        )));

        assertNoBusinessLog(output);
        assertNoSensitiveData(output);
    }

    private void assertNoBusinessLog(CapturedOutput output) {
        assertThat(output).doesNotContain("criado com sucesso");
    }

    private void assertNoSensitiveData(CapturedOutput output) {
        String out = output.toString().toLowerCase();
        assertThat(out).doesNotContain("password");
        assertThat(out).doesNotContain("secret");
        assertThat(out).doesNotContain("student@estudante.sesisenai.org.br");
        assertThat(out).doesNotContain("duplicate@estudante.sesisenai.org.br");
        assertThat(out).doesNotContain("activation-token");
    }
}
