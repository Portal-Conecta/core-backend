package com.portal.conecta.hub.module.classes.application.use_case;

import com.portal.conecta.hub.module.classes.domain.exception.ClassEntityNotFoundException;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DeleteClassUseCaseTest {

    @Mock
    private ClassRepository classRepository;

    @Mock
    private ClassPermissionValidator permissionValidator;

    @Mock
    private RequestContextProvider requestProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClassEventPublisher classEventPublisher;

    @InjectMocks
    private DeleteClassUseCase useCase;

    private UUID userId;
    private UUID classId;
    private UserEntity deletedBy;
    private ClassEntity classEntity;
    private RequestContext context;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        classId = UUID.randomUUID();

        deletedBy = new UserEntity("Deleter", "deleter@test.com", "hash", TypeUser.SENAI);
        classEntity = spy(new ClassEntity(Shift.FULL_AM_PM, 1, "MIDS1", new CourseEntity("Desenvolvimento de Sistemas", "MIDS")));

        context = new RequestContext(userId, TypeUser.SENAI, List.of());

        when(requestProvider.getRequestContext()).thenReturn(context);
    }

    @Test
    @DisplayName("deve remover turma logicamente com sucesso")
    void shouldDeleteClassSuccessfully() {
        doNothing().when(permissionValidator).validateCanDelete(TypeUser.SENAI);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(userId)).thenReturn(Optional.of(deletedBy));
        when(classRepository.save(any(ClassEntity.class))).thenAnswer(i -> i.getArgument(0));

        assertThatCode(() -> useCase.execute(classId)).doesNotThrowAnyException();

        verify(classRepository).save(classEntity);
        verify(classEntity).delete(deletedBy);
        verify(classEventPublisher).publishDeleted(classEntity);
    }

    @Test
    @DisplayName("deve ser idempotente quando turma já está removida")
    void shouldBeIdempotentWhenAlreadyDeleted() {
        classEntity.delete(deletedBy); // deleta primeiro

        doNothing().when(permissionValidator).validateCanDelete(TypeUser.SENAI);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(userId)).thenReturn(Optional.of(deletedBy));
        when(classRepository.save(any(ClassEntity.class))).thenAnswer(i -> i.getArgument(0));

        assertThatCode(() -> useCase.execute(classId)).doesNotThrowAnyException();

        verify(classRepository).save(classEntity);
        verify(classEventPublisher).publishDeleted(classEntity);
    }

    @Test
    @DisplayName("deve lançar UserPermissionDeniedException quando usuário não tem permissão")
    void shouldThrowWhenUserHasNoPermission() {
        doThrow(new UserPermissionDeniedException("no permission"))
                .when(permissionValidator).validateCanDelete(TypeUser.SENAI);

        assertThatThrownBy(() -> useCase.execute(classId))
                .isInstanceOf(UserPermissionDeniedException.class);

        verifyNoInteractions(classRepository, userRepository, classEventPublisher);
    }

    @Test
    @DisplayName("deve lançar ClassEntityNotFoundException quando turma não existe")
    void shouldThrowWhenClassNotFound() {
        doNothing().when(permissionValidator).validateCanDelete(TypeUser.SENAI);
        when(classRepository.findById(classId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(classId))
                .isInstanceOf(ClassEntityNotFoundException.class);

        verifyNoInteractions(userRepository, classEventPublisher);
        verify(classRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve lançar UserNotFoundException quando usuário autenticado não existe no banco")
    void shouldThrowWhenAuthenticatedUserNotFound() {
        doNothing().when(permissionValidator).validateCanDelete(TypeUser.SENAI);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(classId))
                .isInstanceOf(UserNotFoundException.class);

        verify(classRepository, never()).save(any());
        verifyNoInteractions(classEventPublisher);
    }

    @Test
    @DisplayName("não deve salvar quando permissão é negada")
    void shouldNotSaveWhenUnauthorized() {
        doThrow(new UserPermissionDeniedException("no permission"))
                .when(permissionValidator).validateCanDelete(TypeUser.SENAI);

        assertThatThrownBy(() -> useCase.execute(classId))
                .isInstanceOf(UserPermissionDeniedException.class);

        verify(classRepository, never()).save(any());
        verifyNoInteractions(classEventPublisher);
    }
}