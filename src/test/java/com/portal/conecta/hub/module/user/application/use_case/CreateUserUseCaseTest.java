package com.portal.conecta.hub.module.user.application.use_case;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.DisplayName;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import static org.assertj.core.api.Assertions.assertThat;
import com.portal.conecta.hub.module.user.application.command.CreateUserCommand;
import com.portal.conecta.hub.module.user.domain.exception.EmailAlreadyInUseException;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.module.user.domain.policy.UserEmailPolicy;
import com.portal.conecta.hub.module.user.domain.validator.UserPermissionValidator;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RequestContextProvider contextProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    private CreateUserUseCase useCase;

    @BeforeEach
    void setUp() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger("com.portal.conecta.hub.module.user.application.use_case").setLevel(Level.INFO);

        useCase = new CreateUserUseCase(
                userRepository,
                new UserEmailPolicy(),
                new UserPermissionValidator(),
                contextProvider,
                passwordEncoder
        );
    }

    @Test
    void createsUserWithNormalizedDataAndPasswordHash() {
        UUID adminId = UUID.randomUUID();
        UserEntity creator = new UserEntity("Admin", "admin@portal.test", "admin-hash", TypeUser.ADMIN);

        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(adminId, TypeUser.ADMIN, List.of()));
        when(userRepository.existsByEmailIgnoreCase("student@estudante.sesisenai.org.br")).thenReturn(false);
        when(userRepository.findById(adminId)).thenReturn(Optional.of(creator));
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity result = useCase.execute(new CreateUserCommand(
                "  Student One  ",
                "STUDENT@ESTUDANTE.SESISENAI.ORG.BR ",
                "secret",
                TypeUser.STUDENT
        ));

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());

        UserEntity savedUser = userCaptor.getValue();
        assertEquals("Student One", savedUser.getName());
        assertEquals("student@estudante.sesisenai.org.br", savedUser.getEmail());
        assertEquals("encoded-secret", savedUser.getPasswordHash());
        assertEquals(TypeUser.STUDENT, savedUser.getTypeUser());
        assertEquals(creator, savedUser.getCreatedBy());
        assertTrue(savedUser.isActive());
        assertNull(savedUser.getDeletedAt());

        assertEquals(savedUser.getId(), result.getId());
        assertEquals("Student One", result.getName());
        assertEquals("student@estudante.sesisenai.org.br", result.getEmail());
        assertEquals(TypeUser.STUDENT, result.getTypeUser());
        assertTrue(result.isActive());
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
                "secret",
                TypeUser.STUDENT
        )));

        verify(userRepository, never()).findById(any(UUID.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
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
                "secret",
                TypeUser.STUDENT
        )));

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void rejectsUserWithoutPermissionBeforeCheckingEmail() {
        UUID wegId = UUID.randomUUID();

        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(wegId, TypeUser.WEG, List.of()));

        assertThrows(UserPermissionDeniedException.class, () -> useCase.execute(new CreateUserCommand(
                "Teacher",
                "teacher@estudante.sesisenai.org.br",
                "secret",
                TypeUser.TEACHER
        )));

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void rejectsEmailOutsideSenaiOrWegDomainsBeforeEncodingOrSaving() {
        UUID adminId = UUID.randomUUID();
        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(adminId, TypeUser.ADMIN, List.of()));

        assertThrows(InvalidUserDataException.class, () -> useCase.execute(new CreateUserCommand(
                "External",
                "external@example.com",
                "secret",
                TypeUser.STUDENT
        )));

        verify(userRepository, never()).existsByEmailIgnoreCase(anyString());
        verify(userRepository, never()).findById(any(UUID.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("deve emitir INFO com targetUserId e targetUserType após salvar usuário")
    void shouldEmitInfoLogAfterSave(CapturedOutput output) {
        UUID adminId = UUID.randomUUID();
        UserEntity creator = new UserEntity("Admin", "admin@portal.test", "admin-hash", TypeUser.ADMIN);

        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(adminId, TypeUser.ADMIN, List.of()));
        when(userRepository.existsByEmailIgnoreCase("student@estudante.sesisenai.org.br")).thenReturn(false);
        when(userRepository.findById(adminId)).thenReturn(Optional.of(creator));
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");

        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            org.springframework.test.util.ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
            return user;
        });

        UserEntity result = useCase.execute(new CreateUserCommand(
                "Student One",
                "student@estudante.sesisenai.org.br",
                "secret",
                TypeUser.STUDENT
        ));

        assertThat(output).contains("Usuário criado com sucesso. targetUserId=");
        assertThat(output).contains(result.getId().toString()); // Deve conter o ID da entidade afetada
        assertThat(output).contains("targetUserType=STUDENT");
        assertThat(output).doesNotContain(adminId.toString()); // Não deve mais conter o requesterUserId

        assertNoSensitiveData(output);
    }

    @Test
    @DisplayName("deve lançar EmailAlreadyInUseException sem log de negócio quando e-mail já está em uso")
    void shouldThrowWithoutBusinessLogWhenEmailAlreadyInUse(CapturedOutput output) {
        UUID adminId = UUID.randomUUID();

        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(adminId, TypeUser.ADMIN, List.of()));
        when(userRepository.existsByEmailIgnoreCase("duplicate@estudante.sesisenai.org.br")).thenReturn(true);

        assertThrows(EmailAlreadyInUseException.class, () -> useCase.execute(new CreateUserCommand(
                "Duplicate",
                "duplicate@estudante.sesisenai.org.br",
                "secret",
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
    }
}
