package com.portal.conecta.hub.module.course.application.use_case;

import com.portal.conecta.hub.module.course.application.command.CreateCourseCommand;
import com.portal.conecta.hub.module.course.domain.exception.CourseCodeAlreadyInUseException;
import com.portal.conecta.hub.module.course.domain.exception.CourseNameAlreadyInUseException;
import com.portal.conecta.hub.module.course.domain.model.CourseEntity;
import com.portal.conecta.hub.module.course.domain.port.CourseEventPublisher;
import com.portal.conecta.hub.module.course.domain.port.CourseRepository;
import com.portal.conecta.hub.module.course.domain.validator.CoursePermissionValidator;
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
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateCourseUseCaseTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CoursePermissionValidator permissionValidator;

    @Mock
    private RequestContextProvider requestProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseEventPublisher courseEventPublisher;

    @InjectMocks
    private CreateCourseUseCase useCase;

    private UUID userId;
    private UserEntity user;
    private RequestContext context;
    private CreateCourseCommand command;

    @BeforeEach
    void setUp() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger("com.portal.conecta.hub.module.course.application.use_case").setLevel(Level.INFO);

        userId = UUID.randomUUID();
        user = new UserEntity("User Test", "user@test.com", "hash", TypeUser.SENAI);
        context = new RequestContext(userId, TypeUser.SENAI, List.of());
        command = new CreateCourseCommand("Desenvolvimento de Sistemas", "DS");
    }

    @Test
    @DisplayName("deve criar curso com sucesso")
    void shouldCreateCourseSuccessfully() {
        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canCreate(TypeUser.SENAI)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.existsByName(command.name())).thenReturn(false);
        when(courseRepository.existsByCode(command.code())).thenReturn(false);
        when(courseRepository.save(any(CourseEntity.class))).thenAnswer(i -> i.getArgument(0));

        CourseEntity result = useCase.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Desenvolvimento de Sistemas");
        assertThat(result.getCode()).isEqualTo("DS");
        assertThat(result.getCreatedBy()).isEqualTo(user);
        assertThat(result.getDeletedAt()).isNull();

        verify(courseRepository).save(any(CourseEntity.class));
        verify(courseEventPublisher).publishCreated(any(CourseEntity.class));
    }

    @Test
    @DisplayName("deve lançar UnauthorizedUserException quando usuário não tem permissão")
    void shouldThrowWhenUserIsNotAuthorized() {
        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canCreate(TypeUser.SENAI)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserPermissionDeniedException.class);

        verifyNoInteractions(userRepository, courseRepository, courseEventPublisher);
    }

    @Test
    @DisplayName("deve lançar UserNotFoundException quando usuário do contexto não existe")
    void shouldThrowWhenUserNotFound() {
        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canCreate(TypeUser.SENAI)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFoundException.class);

        verify(courseRepository, never()).save(any());
        verifyNoInteractions(courseEventPublisher);
    }

    @Test
    @DisplayName("deve lançar CourseNameAlreadyInUseException quando name já está em uso")
    void shouldThrowWhenNameAlreadyInUse() {
        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canCreate(TypeUser.SENAI)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.existsByName(command.name())).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(CourseNameAlreadyInUseException.class)
                .hasMessageContaining(command.name());

        verify(courseRepository, never()).save(any());
        verifyNoInteractions(courseEventPublisher);
    }

    @Test
    @DisplayName("deve lançar CourseCodeAlreadyInUseException quando code já está em uso")
    void shouldThrowWhenCodeAlreadyInUse() {
        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canCreate(TypeUser.SENAI)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.existsByName(command.name())).thenReturn(false);
        when(courseRepository.existsByCode(command.code())).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(CourseCodeAlreadyInUseException.class)
                .hasMessageContaining(command.code());

        verify(courseRepository, never()).save(any());
        verifyNoInteractions(courseEventPublisher);
    }

    @Test
    @DisplayName("não deve salvar curso quando permissão é negada")
    void shouldNotSaveWhenUnauthorized() {
        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canCreate(TypeUser.SENAI)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserPermissionDeniedException.class);

        verify(courseRepository, never()).save(any());
        verifyNoInteractions(courseEventPublisher);
    }

    @Test
    @DisplayName("STUDENT não pode criar curso")
    void shouldThrowWhenUserIsStudent() {
        RequestContext studentContext = new RequestContext(userId, TypeUser.STUDENT, List.of());
        when(requestProvider.getRequestContext()).thenReturn(studentContext);
        when(permissionValidator.canCreate(TypeUser.STUDENT)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserPermissionDeniedException.class);

        verify(courseRepository, never()).save(any());
        verifyNoInteractions(courseEventPublisher);
    }

    @Test
    @DisplayName("TEACHER não pode criar curso")
    void shouldThrowWhenUserIsTeacher() {
        RequestContext teacherContext = new RequestContext(userId, TypeUser.TEACHER, List.of());
        when(requestProvider.getRequestContext()).thenReturn(teacherContext);
        when(permissionValidator.canCreate(TypeUser.TEACHER)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserPermissionDeniedException.class);

        verify(courseRepository, never()).save(any());
        verifyNoInteractions(courseEventPublisher);
    }

    @Test
    @DisplayName("deve emitir INFO com courseId e courseCode após criar curso")
    void shouldEmitInfoLogAfterSave(CapturedOutput output) {
        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canCreate(TypeUser.SENAI)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.existsByName(command.name())).thenReturn(false);
        when(courseRepository.existsByCode(command.code())).thenReturn(false);

        when(courseRepository.save(any(CourseEntity.class))).thenAnswer(invocation -> {
            CourseEntity savedCourse = invocation.getArgument(0);
            org.springframework.test.util.ReflectionTestUtils.setField(savedCourse, "id", java.util.UUID.randomUUID());
            return savedCourse;
        });

        CourseEntity result = useCase.execute(command);

        assertThat(output).contains("criado com sucesso");
        assertThat(output).contains(result.getId().toString()); // Deve conter o ID do curso afetado
        assertThat(output).contains("DS"); // Assumindo que "DS" é o código passado no seu command
        assertThat(output).doesNotContain(userId.toString());

        assertNoSensitiveData(output);
    }

    @Test
    @DisplayName("deve lançar CourseNameAlreadyInUseException sem log de negócio quando nome já está em uso")
    void shouldThrowWithoutBusinessLogWhenNameAlreadyInUse(CapturedOutput output) {
        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canCreate(TypeUser.SENAI)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.existsByName(command.name())).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(CourseNameAlreadyInUseException.class);

        assertNoBusinessLog(output);
        assertNoSensitiveData(output);
    }

    @Test
    @DisplayName("deve lançar UserPermissionDeniedException sem log de negócio quando permissão é negada")
    void shouldThrowWithoutBusinessLogWhenPermissionDenied(CapturedOutput output) {
        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canCreate(TypeUser.SENAI)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserPermissionDeniedException.class);

        assertNoBusinessLog(output);
        assertNoSensitiveData(output);
    }

    private void assertNoBusinessLog(CapturedOutput output) {
        assertThat(output).doesNotContain("criado com sucesso");
    }

    private void assertNoSensitiveData(CapturedOutput output) {
        String out = output.toString().toLowerCase();
        assertThat(out).doesNotContain("authorization");
        assertThat(out).doesNotContain("user@test.com");
        assertThat(out).doesNotContain("hash");
    }
}