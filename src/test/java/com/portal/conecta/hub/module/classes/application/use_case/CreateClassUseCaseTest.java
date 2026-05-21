package com.portal.conecta.hub.module.classes.application.use_case;

import com.portal.conecta.hub.module.classes.application.command.CreateClassCommand;
import com.portal.conecta.hub.module.classes.domain.exception.CourseNotFoundException;
import com.portal.conecta.hub.module.classes.domain.exception.InvalidClassDataException;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.model.Shift;
import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
import com.portal.conecta.hub.module.classes.domain.validator.ClassPermissionValidator;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.course.domain.port.CourseRepository;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import com.portal.conecta.hub.shared.exception.UnauthorizedUserException;
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
class CreateClassUseCaseTest {

    @Mock
    private ClassRepository classRepository;

    @Mock
    private ClassPermissionValidator permissionValidator;

    @Mock
    private RequestContextProvider requestProvider;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CreateClassUseCase useCase;

    private UUID userId;
    private UUID courseId;
    private UserEntity user;
    private CourseEntity course;
    private RequestContext context;
    private CreateClassCommand command;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        courseId = UUID.randomUUID();

        user = new UserEntity("User Test", "user@test.com", "hash", TypeUser.SENAI);
        course = new CourseEntity("Desenvolvimento de Sistemas", "MIDS");
        context = new RequestContext(userId, TypeUser.SENAI, List.of());
        command = new CreateClassCommand(Shift.FULL_AM_PM, courseId);
    }

    @Test
    @DisplayName("deve criar turma com sucesso quando não há turmas no curso — número começa em 1")
    void shouldCreateClassSuccessfullyWithFirstNumber() {
        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canCreate(TypeUser.SENAI)).thenReturn(true);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(classRepository.findLastNumberByCourseId(courseId)).thenReturn(Optional.empty());
        when(classRepository.save(any(ClassEntity.class))).thenAnswer(i -> i.getArgument(0));

        ClassEntity result = useCase.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.getShift()).isEqualTo(Shift.FULL_AM_PM);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("MIDS1");
        assertThat(result.getCourse()).isEqualTo(course);
        assertThat(result.getCreatedBy()).isEqualTo(user);

        verify(classRepository).save(any(ClassEntity.class));
    }

    @Test
    @DisplayName("deve criar turma com número incrementado quando já existem turmas no curso")
    void shouldCreateClassWithIncrementedNumber() {
        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canCreate(TypeUser.SENAI)).thenReturn(true);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(classRepository.findLastNumberByCourseId(courseId)).thenReturn(Optional.of(77));
        when(classRepository.save(any(ClassEntity.class))).thenAnswer(i -> i.getArgument(0));

        ClassEntity result = useCase.execute(command);

        assertThat(result.getNumber()).isEqualTo(78);
        assertThat(result.getName()).isEqualTo("MIDS78");

        verify(classRepository).save(any(ClassEntity.class));
    }

    @Test
    @DisplayName("deve lançar InvalidClassDataException quando command é nulo")
    void shouldThrowWhenCommandIsNull() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(InvalidClassDataException.class);

        verifyNoInteractions(requestProvider, permissionValidator, courseRepository,
                userRepository, classRepository);
    }

    @Test
    @DisplayName("deve lançar UnauthorizedUserException quando usuário não tem permissão")
    void shouldThrowWhenUserIsNotAuthorized() {
        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canCreate(TypeUser.SENAI)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UnauthorizedUserException.class);

        verifyNoInteractions(courseRepository, userRepository, classRepository);
    }

    @Test
    @DisplayName("deve lançar CourseNotFoundException quando curso não existe")
    void shouldThrowWhenCourseNotFound() {
        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canCreate(TypeUser.SENAI)).thenReturn(true);
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(CourseNotFoundException.class)
                .hasMessageContaining(courseId.toString());

        verifyNoInteractions(userRepository, classRepository);
    }

    @Test
    @DisplayName("deve lançar UserNotFoundException quando usuário do contexto não existe")
    void shouldThrowWhenUserNotFound() {
        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canCreate(TypeUser.SENAI)).thenReturn(true);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(userId.toString());

        verifyNoInteractions(classRepository);
    }

    @Test
    @DisplayName("não deve salvar turma quando permissão é negada")
    void shouldNotSaveWhenUnauthorized() {
        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canCreate(TypeUser.SENAI)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UnauthorizedUserException.class);

        verify(classRepository, never()).save(any());
    }
}