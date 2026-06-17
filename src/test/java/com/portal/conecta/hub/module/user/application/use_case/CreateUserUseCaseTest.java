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

@ExtendWith(MockitoExtension.class)
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
}
