package com.portal.conecta.hub.module.course.application.use_case;

import com.portal.conecta.hub.module.course.application.command.UpdateCourseCommand;
import com.portal.conecta.hub.module.course.domain.exception.*;
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
import org.springframework.test.util.ReflectionTestUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateCourseUseCaseTest {

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
    private UpdateCourseUseCase useCase;

    private UUID userId;
    private UUID courseId;
    private UserEntity user;
    private CourseEntity course;
    private RequestContext context;
    private UpdateCourseCommand command;

    @BeforeEach
    void setUp() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger("com.portal.conecta.hub.module.course.application.use_case").setLevel(Level.INFO);

        userId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        user = new UserEntity("User Test", "user@test.com", "hash", TypeUser.SENAI);
        course = new CourseEntity("Desenvolvimento de Sistemas", "DS");
        ReflectionTestUtils.setField(course, "id", courseId);
        context = new RequestContext(userId, TypeUser.SENAI, List.of());
        command = UpdateCourseCommand.of(courseId, "Novo Nome", "NN");
    }

    // ==================== COMMAND VALIDATION ====================

    @Test
    @DisplayName("deve lançar InvalidCourseDataException quando name e code são nulos")
    void shouldThrowWhenBothFieldsAreNull() {
        assertThatThrownBy(() -> UpdateCourseCommand.of(courseId, null, null))
                .isInstanceOf(InvalidCourseDataException.class);
    }

    @Test
    @DisplayName("deve lançar InvalidCourseDataException quando name é blank")
    void shouldThrowWhenNameIsBlank() {
        assertThatThrownBy(() -> UpdateCourseCommand.of(courseId, "", null))
                .isInstanceOf(InvalidCourseDataException.class);
    }

    @Test
    @DisplayName("deve lançar InvalidCourseDataException quando name é whitespace")
    void shouldThrowWhenNameIsWhitespace() {
        assertThatThrownBy(() -> UpdateCourseCommand.of(courseId, "   ", null))
                .isInstanceOf(InvalidCourseDataException.class);
    }

    @Test
    @DisplayName("deve lançar InvalidCourseDataException quando code é blank")
    void shouldThrowWhenCodeIsBlank() {
        assertThatThrownBy(() -> UpdateCourseCommand.of(courseId, null, ""))
                .isInstanceOf(InvalidCourseDataException.class);
    }

    @Test
    @DisplayName("deve lançar InvalidCourseDataException quando code é whitespace")
    void shouldThrowWhenCodeIsWhitespace() {
        assertThatThrownBy(() -> UpdateCourseCommand.of(courseId, null, "   "))
                .isInstanceOf(InvalidCourseDataException.class);
    }

    @Test
    @DisplayName("deve lançar InvalidCourseDataException quando courseId é nulo")
    void shouldThrowWhenCourseIdIsNull() {
        assertThatThrownBy(() -> UpdateCourseCommand.of(null, "Novo Nome", "NN"))
                .isInstanceOf(InvalidCourseDataException.class);
    }

    // ==================== USE CASE ====================

    @Test
    @DisplayName("deve atualizar curso com sucesso")
    void shouldUpdateCourseSuccessfully() {
        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canUpdate(TypeUser.SENAI)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.existsByNameAndIdNot("Novo Nome", courseId)).thenReturn(false);
        when(courseRepository.existsByCodeAndIdNot("NN", courseId)).thenReturn(false);
        when(courseRepository.save(any(CourseEntity.class))).thenAnswer(i -> i.getArgument(0));

        CourseEntity result = useCase.execute(command);

        assertThat(result.getName()).isEqualTo("Novo Nome");
        assertThat(result.getCode()).isEqualTo("NN");
        assertThat(result.getUpdatedBy()).isEqualTo(user);
        verify(courseRepository).save(course);
        verify(courseEventPublisher).publishUpdated(course);
    }

    @Test
    @DisplayName("deve atualizar apenas name quando code não é informado")
    void shouldUpdateOnlyNameWhenCodeIsNull() {
        UpdateCourseCommand partialCommand = UpdateCourseCommand.of(courseId, "Novo Nome", null);

        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canUpdate(TypeUser.SENAI)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.existsByNameAndIdNot("Novo Nome", courseId)).thenReturn(false);
        when(courseRepository.save(any(CourseEntity.class))).thenAnswer(i -> i.getArgument(0));

        CourseEntity result = useCase.execute(partialCommand);

        assertThat(result.getName()).isEqualTo("Novo Nome");
        assertThat(result.getCode()).isEqualTo("DS");
        verify(courseRepository).save(course);
        verify(courseEventPublisher).publishUpdated(course);
    }

    @Test
    @DisplayName("deve atualizar apenas code quando name não é informado")
    void shouldUpdateOnlyCodeWhenNameIsNull() {
        UpdateCourseCommand partialCommand = UpdateCourseCommand.of(courseId, null, "NN");

        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canUpdate(TypeUser.SENAI)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.existsByCodeAndIdNot("NN", courseId)).thenReturn(false);
        when(courseRepository.save(any(CourseEntity.class))).thenAnswer(i -> i.getArgument(0));

        CourseEntity result = useCase.execute(partialCommand);

        assertThat(result.getName()).isEqualTo("Desenvolvimento de Sistemas");
        assertThat(result.getCode()).isEqualTo("NN");
        verify(courseRepository).save(course);
        verify(courseEventPublisher).publishUpdated(course);
    }

    @Test
    @DisplayName("deve lançar UserPermissionDeniedException quando usuário não tem permissão")
    void shouldThrowWhenUserIsNotAuthorized() {
        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canUpdate(TypeUser.SENAI)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserPermissionDeniedException.class);

        verifyNoInteractions(userRepository, courseRepository, courseEventPublisher);
    }

    @Test
    @DisplayName("deve lançar UserNotFoundException quando usuário do contexto não existe")
    void shouldThrowWhenUserNotFound() {
        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canUpdate(TypeUser.SENAI)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFoundException.class);

        verify(courseRepository, never()).save(any());
        verifyNoInteractions(courseEventPublisher);
    }

    @Test
    @DisplayName("deve lançar CourseEntityNotFoundException quando curso não existe")
    void shouldThrowWhenCourseNotFound() {
        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canUpdate(TypeUser.SENAI)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(CourseNotFoundException.class);

        verify(courseRepository, never()).save(any());
        verifyNoInteractions(courseEventPublisher);
    }

    @Test
    @DisplayName("deve lançar DeletedCourseException quando curso está removido logicamente")
    void shouldThrowWhenCourseIsDeleted() {
        ReflectionTestUtils.setField(course, "deletedAt", Instant.now());

        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canUpdate(TypeUser.SENAI)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(DeletedCourseException.class);

        verify(courseRepository, never()).save(any());
        verifyNoInteractions(courseEventPublisher);
    }

    @Test
    @DisplayName("deve lançar CourseNameAlreadyInUseException quando name já está em uso por outro curso")
    void shouldThrowWhenNameAlreadyInUse() {
        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canUpdate(TypeUser.SENAI)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.existsByNameAndIdNot("Novo Nome", courseId)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(CourseNameAlreadyInUseException.class)
                .hasMessageContaining("Novo Nome");

        verify(courseRepository, never()).save(any());
        verifyNoInteractions(courseEventPublisher);
    }

    @Test
    @DisplayName("deve lançar CourseCodeAlreadyInUseException quando code já está em uso por outro curso")
    void shouldThrowWhenCodeAlreadyInUse() {
        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canUpdate(TypeUser.SENAI)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.existsByNameAndIdNot("Novo Nome", courseId)).thenReturn(false);
        when(courseRepository.existsByCodeAndIdNot("NN", courseId)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(CourseCodeAlreadyInUseException.class)
                .hasMessageContaining("NN");

        verify(courseRepository, never()).save(any());
        verifyNoInteractions(courseEventPublisher);
    }

    @Test
    @DisplayName("STUDENT não pode editar curso")
    void shouldThrowWhenUserIsStudent() {
        RequestContext studentContext = new RequestContext(userId, TypeUser.STUDENT, List.of());
        when(requestProvider.getRequestContext()).thenReturn(studentContext);
        when(permissionValidator.canUpdate(TypeUser.STUDENT)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserPermissionDeniedException.class);

        verify(courseRepository, never()).save(any());
        verifyNoInteractions(courseEventPublisher);
    }

    @Test
    @DisplayName("deve emitir INFO com courseId e changedFields após atualizar curso")
    void shouldEmitInfoLogAfterUpdate(CapturedOutput output) {
        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canUpdate(TypeUser.SENAI)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.existsByNameAndIdNot("Novo Nome", courseId)).thenReturn(false);
        when(courseRepository.existsByCodeAndIdNot("NN", courseId)).thenReturn(false);
        when(courseRepository.save(any(CourseEntity.class))).thenAnswer(i -> i.getArgument(0));

        useCase.execute(command);

        assertThat(output).contains("atualizado com sucesso");
        assertThat(output).contains(courseId.toString());
        assertThat(output).contains("name");
        assertThat(output).contains("code");
        assertThat(output).doesNotContain(userId.toString());
        assertNoSensitiveData(output);
    }

    @Test
    @DisplayName("deve emitir changedFields apenas com name quando somente nome muda")
    void shouldEmitOnlyNameInChangedFieldsWhenOnlyNameChanges(CapturedOutput output) {
        UpdateCourseCommand partialCommand = UpdateCourseCommand.of(courseId, "Novo Nome", null);

        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canUpdate(TypeUser.SENAI)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.existsByNameAndIdNot("Novo Nome", courseId)).thenReturn(false);
        when(courseRepository.save(any(CourseEntity.class))).thenAnswer(i -> i.getArgument(0));

        useCase.execute(partialCommand);

        assertThat(output).contains("atualizado com sucesso");
        assertThat(output).contains("name");
        assertThat(output).doesNotContain("[name, code]");
        assertNoSensitiveData(output);
    }

    @Test
    @DisplayName("deve lançar UserPermissionDeniedException sem log de negócio quando permissão é negada")
    void shouldThrowWithoutBusinessLogWhenPermissionDenied(CapturedOutput output) {
        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canUpdate(TypeUser.SENAI)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserPermissionDeniedException.class);

        assertNoBusinessLog(output);
        assertNoSensitiveData(output);
    }

    @Test
    @DisplayName("deve lançar CourseNotFoundException sem log de negócio quando curso não existe")
    void shouldThrowWithoutBusinessLogWhenCourseNotFound(CapturedOutput output) {
        when(requestProvider.getRequestContext()).thenReturn(context);
        when(permissionValidator.canUpdate(TypeUser.SENAI)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(CourseNotFoundException.class);

        assertNoBusinessLog(output);
        assertNoSensitiveData(output);
    }

    private void assertNoBusinessLog(CapturedOutput output) {
        assertThat(output).doesNotContain("atualizado com sucesso");
    }

    private void assertNoSensitiveData(CapturedOutput output) {
        String out = output.toString().toLowerCase();
        assertThat(out).doesNotContain("authorization");
        assertThat(out).doesNotContain("user@test.com");
        assertThat(out).doesNotContain("hash");
        assertThat(out).doesNotContain("novo nome");
        assertThat(out).doesNotContain("desenvolvimento de sistemas");
    }
}