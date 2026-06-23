package com.portal.conecta.hub.module.classes.application.use_case;

import com.portal.conecta.hub.module.classes.domain.exception.ClassEntityNotFoundException;
import com.portal.conecta.hub.module.classes.domain.exception.InvalidClassDataException;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.model.Shift;
import com.portal.conecta.hub.module.classes.domain.port.ClassEventPublisher;
import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
import com.portal.conecta.hub.module.classes.domain.validator.ClassPermissionValidator;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactivateClassUseCaseTest {

    @Mock private ClassRepository classRepository;
    @Mock private UserRepository userRepository;
    @Mock private ClassPermissionValidator permissionValidator;
    @Mock private RequestContextProvider contextProvider;
    @Mock private ClassEventPublisher classEventPublisher;

    @InjectMocks
    private ReactivateClassUseCase useCase;

    private UUID classId;
    private UUID userId;
    private UserEntity executor;
    private ClassEntity classEntity;
    private RequestContext context;

    @BeforeEach
    void setUp() {
        classId = UUID.randomUUID();
        userId = UUID.randomUUID();

        executor = new UserEntity("Admin", "admin@test.com", "hash", TypeUser.ADMIN);
        context = new RequestContext(userId, TypeUser.ADMIN, List.of());

        CourseEntity course = new CourseEntity("Desenvolvimento de Sistemas", "MIDS");
        classEntity = ClassEntity.create(Shift.FULL_AM_PM, 1, course, executor);
        classEntity.deactivate(executor);
    }

    @Test
    @DisplayName("deve reativar turma inativa com sucesso")
    void shouldReactivateInactiveClassSuccessfully() {
        when(contextProvider.getRequestContext()).thenReturn(context);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(userId)).thenReturn(Optional.of(executor));
        when(classRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ClassEntity result = useCase.execute(classId);

        assertThat(result.isActive()).isTrue();
        verify(classRepository).save(classEntity);
        verify(classEventPublisher).publishCreated(classEntity);
    }

    @Test
    @DisplayName("deve lançar NullPointerException quando classId é nulo")
    void shouldThrowWhenClassIdIsNull() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(contextProvider, permissionValidator, classRepository, userRepository, classEventPublisher);
    }

    @Test
    @DisplayName("deve lançar UserPermissionDeniedException quando usuário não tem permissão")
    void shouldThrowWhenUserHasNoPermission() {
        when(contextProvider.getRequestContext()).thenReturn(context);
        doThrow(new UserPermissionDeniedException("Usuário não tem permissão para reativar uma turma."))
                .when(permissionValidator).validateCanReactivate(TypeUser.ADMIN);

        assertThatThrownBy(() -> useCase.execute(classId))
                .isInstanceOf(UserPermissionDeniedException.class);

        verifyNoInteractions(classRepository, userRepository, classEventPublisher);
    }

    @Test
    @DisplayName("deve lançar ClassEntityNotFoundException quando turma não existe")
    void shouldThrowWhenClassNotFound() {
        when(contextProvider.getRequestContext()).thenReturn(context);
        when(classRepository.findById(classId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(classId))
                .isInstanceOf(ClassEntityNotFoundException.class);

        verifyNoInteractions(userRepository, classEventPublisher);
    }

    @Test
    @DisplayName("deve lançar UserNotFoundException quando executor não existe")
    void shouldThrowWhenExecutorNotFound() {
        when(contextProvider.getRequestContext()).thenReturn(context);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(classId))
                .isInstanceOf(UserNotFoundException.class);

        verify(classRepository, never()).save(any());
        verifyNoInteractions(classEventPublisher);
    }

    @Test
    @DisplayName("deve lançar InvalidClassDataException quando turma já está ativa")
    void shouldThrowWhenClassAlreadyActive() {
        classEntity.reactivate(executor);

        when(contextProvider.getRequestContext()).thenReturn(context);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(userId)).thenReturn(Optional.of(executor));

        assertThatThrownBy(() -> useCase.execute(classId))
                .isInstanceOf(InvalidClassDataException.class)
                .hasMessage("A turma já está ativa.");

        verify(classRepository, never()).save(any());
        verifyNoInteractions(classEventPublisher);
    }

    @Test
    @DisplayName("não deve salvar quando permissão é negada")
    void shouldNotSaveWhenPermissionDenied() {
        when(contextProvider.getRequestContext()).thenReturn(context);
        doThrow(new UserPermissionDeniedException("sem permissão"))
                .when(permissionValidator).validateCanReactivate(TypeUser.ADMIN);

        assertThatThrownBy(() -> useCase.execute(classId))
                .isInstanceOf(UserPermissionDeniedException.class);

        verify(classRepository, never()).save(any());
        verifyNoInteractions(classEventPublisher);
    }
}