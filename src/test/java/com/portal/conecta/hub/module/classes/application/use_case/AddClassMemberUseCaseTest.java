package com.portal.conecta.hub.module.classes.application.use_case;

import com.portal.conecta.hub.module.classes.application.command.AddMemberCommand;
import com.portal.conecta.hub.module.classes.domain.exception.ClassMembershipException;
import com.portal.conecta.hub.module.classes.domain.exception.ClassEntityNotFoundException;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.classes.domain.model.Shift;
import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.classes.domain.port.ClassRepository;
import com.portal.conecta.hub.module.classes.domain.validator.ClassMembershipValidator;
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
class AddClassMemberUseCaseTest {

    @Mock private RequestContextProvider requestProvider;
    @Mock private ClassRepository classRepository;
    @Mock private UserRepository userRepository;
    @Mock private ClassMembershipRepository membershipRepository;
    @Mock private ClassMembershipValidator membershipValidator;

    @InjectMocks
    private AddClassMemberUseCase useCase;

    private UUID executorId;
    private UUID targetUserId;
    private UUID classId;
    private UserEntity executor;
    private UserEntity targetStudent;
    private UserEntity targetTeacher;
    private ClassEntity classEntity;
    private RequestContext senaiContext;

    @BeforeEach
    void setUp() {
        executorId = UUID.randomUUID();
        targetUserId = UUID.randomUUID();
        classId = UUID.randomUUID();

        executor = new UserEntity("Executor", "executor@test.com", "hash", TypeUser.SENAI);
        targetStudent = new UserEntity("Student", "student@test.com", "hash", TypeUser.STUDENT);
        targetTeacher = new UserEntity("Teacher", "teacher@test.com", "hash", TypeUser.TEACHER);

        CourseEntity course = new CourseEntity("Desenvolvimento de Sistemas", "MIDS");
        classEntity = ClassEntity.create(Shift.FULL_AM_PM, 1, course, executor);

        senaiContext = new RequestContext(executorId, TypeUser.SENAI, List.of());
    }

    // --- casos de sucesso ---

    @Test
    @DisplayName("deve associar aluno à turma com sucesso")
    void shouldAssociateStudentSuccessfully() {
        AddMemberCommand command = new AddMemberCommand(classId, targetUserId, ClassRole.STUDENT);

        when(requestProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetStudent));
        when(membershipRepository.existsByUserIdAndClassId(targetUserId, classId)).thenReturn(false);
        when(membershipRepository.countByUserIdAndClassRole(targetUserId, ClassRole.STUDENT)).thenReturn(0L);
        when(membershipRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ClassMembershipEntity result = useCase.execute(command);

        assertThat(result.getUser()).isEqualTo(targetStudent);
        assertThat(result.getClassEntity()).isEqualTo(classEntity);
        assertThat(result.getClassRole()).isEqualTo(ClassRole.STUDENT);
        verify(membershipRepository).save(any(ClassMembershipEntity.class));
    }

    @Test
    @DisplayName("deve associar docente à turma com sucesso")
    void shouldAssociateTeacherSuccessfully() {
        AddMemberCommand command = new AddMemberCommand(classId, targetUserId, ClassRole.TEACHER);

        when(requestProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetTeacher));
        when(membershipRepository.existsByUserIdAndClassId(targetUserId, classId)).thenReturn(false);
        when(membershipRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ClassMembershipEntity result = useCase.execute(command);

        assertThat(result.getClassRole()).isEqualTo(ClassRole.TEACHER);
        verify(membershipRepository).save(any(ClassMembershipEntity.class));
        verify(membershipRepository, never()).countByUserIdAndClassRole(any(), any());
    }

    @Test
    @DisplayName("deve permitir docente com múltiplas turmas ativas")
    void shouldAllowTeacherWithMultipleActiveClasses() {
        AddMemberCommand command = new AddMemberCommand(classId, targetUserId, ClassRole.TEACHER);

        when(requestProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetTeacher));
        when(membershipRepository.existsByUserIdAndClassId(targetUserId, classId)).thenReturn(false);
        when(membershipRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertThat(useCase.execute(command)).isNotNull();
        verify(membershipRepository, never()).countByUserIdAndClassRole(any(), any());
    }

    @Test
    @DisplayName("ADMIN consegue associar TypeUser.STUDENT com classRole STUDENT")
    void shouldAssociateStudentWhenExecutorIsAdmin() {
        AddMemberCommand command = new AddMemberCommand(classId, targetUserId, ClassRole.STUDENT);
        RequestContext adminContext = new RequestContext(executorId, TypeUser.ADMIN, List.of());

        when(requestProvider.getRequestContext()).thenReturn(adminContext);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetStudent));
        when(membershipRepository.existsByUserIdAndClassId(targetUserId, classId)).thenReturn(false);
        when(membershipRepository.countByUserIdAndClassRole(targetUserId, ClassRole.STUDENT)).thenReturn(0L);
        when(membershipRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertThat(useCase.execute(command).getClassRole()).isEqualTo(ClassRole.STUDENT);
    }

    @Test
    @DisplayName("ADMIN consegue associar TypeUser.TEACHER com classRole TEACHER")
    void shouldAssociateTeacherWhenExecutorIsAdmin() {
        AddMemberCommand command = new AddMemberCommand(classId, targetUserId, ClassRole.TEACHER);
        RequestContext adminContext = new RequestContext(executorId, TypeUser.ADMIN, List.of());

        when(requestProvider.getRequestContext()).thenReturn(adminContext);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetTeacher));
        when(membershipRepository.existsByUserIdAndClassId(targetUserId, classId)).thenReturn(false);
        when(membershipRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertThat(useCase.execute(command).getClassRole()).isEqualTo(ClassRole.TEACHER);
    }

    // --- falhas de permissão ---

    @Test
    @DisplayName("deve lançar UserPermissionDeniedException quando executor é WEG")
    void shouldThrowWhenExecutorIsWeg() {
        AddMemberCommand command = new AddMemberCommand(classId, targetUserId, ClassRole.STUDENT);
        RequestContext wegContext = new RequestContext(executorId, TypeUser.WEG, List.of());

        when(requestProvider.getRequestContext()).thenReturn(wegContext);
        doThrow(new UserPermissionDeniedException("sem permissão"))
                .when(membershipValidator).validateExecutorCanAddMember(any(), any(), any(), any());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserPermissionDeniedException.class);

        verifyNoInteractions(classRepository, userRepository, membershipRepository);
    }

    @Test
    @DisplayName("deve lançar ClassMembershipException quando role é REPRESENTATIVE")
    void shouldThrowWhenRoleIsRepresentative() {
        AddMemberCommand command = new AddMemberCommand(classId, targetUserId, ClassRole.REPRESENTATIVE);

        when(requestProvider.getRequestContext()).thenReturn(senaiContext);
        doThrow(new ClassMembershipException("REPRESENTATIVE não permitido"))
                .when(membershipValidator).validateExecutorCanAddMember(any(), any(), any(), any());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ClassMembershipException.class);

        verifyNoInteractions(classRepository, userRepository, membershipRepository);
    }

    // --- falhas de turma ---

    @Test
    @DisplayName("deve lançar ClassEntityNotFoundException quando turma não existe")
    void shouldThrowWhenClassNotFound() {
        AddMemberCommand command = new AddMemberCommand(classId, targetUserId, ClassRole.STUDENT);

        when(requestProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findById(classId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ClassEntityNotFoundException.class)
                .hasMessageContaining(classId.toString());

        verifyNoInteractions(userRepository, membershipRepository);
    }

    @Test
    @DisplayName("deve lançar ClassMembershipException quando turma está deletada")
    void shouldThrowWhenClassIsDeleted() {
        AddMemberCommand command = new AddMemberCommand(classId, targetUserId, ClassRole.STUDENT);

        when(requestProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        classEntity.delete(executor);


        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ClassMembershipException.class);

        verifyNoInteractions(userRepository, membershipRepository);
    }

    // --- falhas de usuário ---

    @Test
    @DisplayName("deve lançar UserNotFoundException quando usuário alvo não existe")
    void shouldThrowWhenUserNotFound() {
        AddMemberCommand command = new AddMemberCommand(classId, targetUserId, ClassRole.STUDENT);

        when(requestProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(targetUserId.toString());

        verifyNoInteractions(membershipRepository);
    }

    @Test
    @DisplayName("deve lançar ClassMembershipException quando usuário alvo está inativo")
    void shouldThrowWhenUserIsInactive() {
        AddMemberCommand command = new AddMemberCommand(classId, targetUserId, ClassRole.STUDENT);

        when(requestProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetStudent));
        doThrow(new ClassMembershipException("User is inactive or deleted."))
                .when(membershipValidator).validateTargetUserCanBeAdded(any(UserEntity.class), any(ClassRole.class));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ClassMembershipException.class);

        verifyNoInteractions(membershipRepository);
    }

    @Test
    @DisplayName("deve lançar ClassMembershipException quando TypeUser do alvo é REPRESENTATIVE")
    void shouldThrowWhenTargetUserIsRepresentative() {
        UserEntity representative = new UserEntity("Rep", "rep@test.com", "hash", TypeUser.REPRESENTATIVE);
        AddMemberCommand command = new AddMemberCommand(classId, targetUserId, ClassRole.STUDENT);

        when(requestProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(representative));
        doThrow(new ClassMembershipException("TypeUser REPRESENTATIVE não pode ser associado"))
                .when(membershipValidator).validateTargetUserCanBeAdded(any(UserEntity.class), any(ClassRole.class));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ClassMembershipException.class);

        verify(membershipRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve lançar ClassMembershipException quando TypeUser do alvo é ADMIN, SENAI ou WEG")
    void shouldThrowWhenTargetUserIsAdministrative() {
        UserEntity adminUser = new UserEntity("Admin", "admin@test.com", "hash", TypeUser.ADMIN);
        AddMemberCommand command = new AddMemberCommand(classId, targetUserId, ClassRole.STUDENT);

        when(requestProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(adminUser));
        doThrow(new ClassMembershipException("TypeUser ADMIN não pode ser associado"))
                .when(membershipValidator).validateTargetUserCanBeAdded(any(UserEntity.class), any(ClassRole.class));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ClassMembershipException.class);

        verify(membershipRepository, never()).save(any());
    }

    // --- falhas de vínculo ---

    @Test
    @DisplayName("deve lançar ClassMembershipException quando vínculo duplicado")
    void shouldThrowWhenMembershipAlreadyExists() {
        AddMemberCommand command = new AddMemberCommand(classId, targetUserId, ClassRole.STUDENT);

        when(requestProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetStudent));
        when(membershipRepository.existsByUserIdAndClassId(targetUserId, classId)).thenReturn(true);
        doThrow(new ClassMembershipException("vínculo duplicado"))
                .when(membershipValidator).validateNoDuplicateMembership(true);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ClassMembershipException.class);

        verify(membershipRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve lançar ClassMembershipException quando aluno já possui turma ativa")
    void shouldThrowWhenStudentAlreadyHasActiveClass() {
        AddMemberCommand command = new AddMemberCommand(classId, targetUserId, ClassRole.STUDENT);

        when(requestProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetStudent));
        when(membershipRepository.existsByUserIdAndClassId(targetUserId, classId)).thenReturn(false);
        when(membershipRepository.countByUserIdAndClassRole(targetUserId, ClassRole.STUDENT)).thenReturn(1L);
        doThrow(new ClassMembershipException("aluno já possui turma ativa"))
                .when(membershipValidator).validateStudentClassLimit(ClassRole.STUDENT, 1L);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ClassMembershipException.class);

        verify(membershipRepository, never()).save(any());
    }
}