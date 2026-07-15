package com.portal.conecta.hub.module.classes.application.use_case;

import com.portal.conecta.hub.module.classes.application.command.BulkAddMembersCommand;
import com.portal.conecta.hub.module.classes.application.use_case.membership.BulkAddClassMembersUseCase;
import com.portal.conecta.hub.module.classes.domain.exception.ClassEntityNotFoundException;
import com.portal.conecta.hub.module.classes.domain.exception.ClassMembershipException;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkAddClassMembersUseCaseTest {

    @Mock private RequestContextProvider requestContextProvider;
    @Mock private ClassRepository classRepository;
    @Mock private UserRepository userRepository;
    @Mock private ClassMembershipRepository classMembershipRepository;
    @Mock private ClassMembershipValidator membershipValidator;

    @InjectMocks
    private BulkAddClassMembersUseCase useCase;

    private UUID executorId;
    private UUID classId;
    private UserEntity executor;
    private UserEntity studentA;
    private UserEntity studentB;
    private UserEntity teacher;
    private ClassEntity classEntity;
    private RequestContext senaiContext;

    @BeforeEach
    void setUp() {
        executorId = UUID.randomUUID();
        classId = UUID.randomUUID();

        executor = new UserEntity("Executor", "executor@sc.senai.br", "hash", TypeUser.SENAI);
        studentA = new UserEntity("Aluno A", "alunoa@estudante.sesisenai.org.br", "hash", TypeUser.STUDENT);
        studentB = new UserEntity("Aluno B", "alunob@estudante.sesisenai.org.br", "hash", TypeUser.STUDENT);
        teacher  = new UserEntity("Professor", "professor@edu.sc.senai.br", "hash", TypeUser.TEACHER);

        CourseEntity course = new CourseEntity("Desenvolvimento de Sistemas", "MIDS");
        classEntity = ClassEntity.create(Shift.FULL_AM_PM, 1, course, executor);
        ReflectionTestUtils.setField(classEntity, "id", classId);

        senaiContext = new RequestContext(executorId, TypeUser.SENAI, List.of());
    }

    // -------------------------------------------------------------------------
    // Sucesso
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deve criar todos os vínculos quando todos os itens são válidos")
    void shouldCreateAllMembershipsWhenAllItemsAreValid() {
        UUID userIdA = UUID.randomUUID();
        UUID userIdB = UUID.randomUUID();

        BulkAddMembersCommand command = new BulkAddMembersCommand(classId, List.of(
                new BulkAddMembersCommand.Item(userIdA, ClassRole.STUDENT),
                new BulkAddMembersCommand.Item(userIdB, ClassRole.STUDENT)
        ));

        when(requestContextProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(userIdA)).thenReturn(Optional.of(studentA));
        when(userRepository.findById(userIdB)).thenReturn(Optional.of(studentB));
        when(classMembershipRepository.existsByUserIdAndClassId(userIdA, classId)).thenReturn(false);
        when(classMembershipRepository.existsByUserIdAndClassId(userIdB, classId)).thenReturn(false);
        when(classMembershipRepository.countByUserIdAndClassRole(userIdA, ClassRole.STUDENT)).thenReturn(0L);
        when(classMembershipRepository.countByUserIdAndClassRole(userIdB, ClassRole.STUDENT)).thenReturn(0L);
        when(classMembershipRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        List<ClassMembershipEntity> result = useCase.execute(command);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ClassMembershipEntity::getClassRole)
                .containsOnly(ClassRole.STUDENT);
        verify(classMembershipRepository, times(2)).save(any(ClassMembershipEntity.class));
    }

    @Test
    @DisplayName("deve criar vínculo de aluno e docente na mesma requisição")
    void shouldCreateStudentAndTeacherInSameRequest() {
        UUID userIdStudent = UUID.randomUUID();
        UUID userIdTeacher = UUID.randomUUID();

        BulkAddMembersCommand command = new BulkAddMembersCommand(classId, List.of(
                new BulkAddMembersCommand.Item(userIdStudent, ClassRole.STUDENT),
                new BulkAddMembersCommand.Item(userIdTeacher, ClassRole.TEACHER)
        ));

        when(requestContextProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(userIdStudent)).thenReturn(Optional.of(studentA));
        when(userRepository.findById(userIdTeacher)).thenReturn(Optional.of(teacher));
        when(classMembershipRepository.existsByUserIdAndClassId(userIdStudent, classId)).thenReturn(false);
        when(classMembershipRepository.existsByUserIdAndClassId(userIdTeacher, classId)).thenReturn(false);
        when(classMembershipRepository.countByUserIdAndClassRole(userIdStudent, ClassRole.STUDENT)).thenReturn(0L);
        when(classMembershipRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        List<ClassMembershipEntity> result = useCase.execute(command);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ClassMembershipEntity::getClassRole)
                .containsExactlyInAnyOrder(ClassRole.STUDENT, ClassRole.TEACHER);
        verify(classMembershipRepository, never()).countByUserIdAndClassRole(userIdTeacher, ClassRole.TEACHER);
    }

    @Test
    @DisplayName("deve criar vÃ­nculo de docente pendente de ativaÃ§Ã£o")
    void shouldCreatePendingTeacherMembership() {
        UUID userIdTeacher = UUID.randomUUID();
        UserEntity pendingTeacher = UserEntity.createPendingActivation(
                "Professor Pendente",
                "pendente@edu.sc.senai.br",
                "hash",
                TypeUser.TEACHER,
                executor
        );

        BulkAddMembersCommand command = new BulkAddMembersCommand(classId, List.of(
                new BulkAddMembersCommand.Item(userIdTeacher, ClassRole.TEACHER)
        ));

        when(requestContextProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(userIdTeacher)).thenReturn(Optional.of(pendingTeacher));
        when(classMembershipRepository.existsByUserIdAndClassId(userIdTeacher, classId)).thenReturn(false);
        when(classMembershipRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        List<ClassMembershipEntity> result = useCase.execute(command);

        assertThat(result).singleElement()
                .extracting(membership -> membership.getUser().isPendingActivation())
                .isEqualTo(true);
        verify(membershipValidator).validateTargetUserCanBeAdded(pendingTeacher, ClassRole.TEACHER);
        verify(classMembershipRepository, never()).countByUserIdAndClassRole(userIdTeacher, ClassRole.TEACHER);
    }

    // -------------------------------------------------------------------------
    // Falhas de turma
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deve lançar ClassEntityNotFoundException quando turma não existe")
    void shouldThrowWhenClassNotFound() {
        BulkAddMembersCommand command = new BulkAddMembersCommand(classId, List.of(
                new BulkAddMembersCommand.Item(UUID.randomUUID(), ClassRole.STUDENT)
        ));

        when(requestContextProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findById(classId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ClassEntityNotFoundException.class);

        verifyNoInteractions(userRepository, classMembershipRepository, membershipValidator);
    }

    @Test
    @DisplayName("deve lançar ClassMembershipException quando turma está deletada")
    void shouldThrowWhenClassIsDeleted() {
        classEntity.delete(executor);

        BulkAddMembersCommand command = new BulkAddMembersCommand(classId, List.of(
                new BulkAddMembersCommand.Item(UUID.randomUUID(), ClassRole.STUDENT)
        ));

        when(requestContextProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ClassMembershipException.class);

        verifyNoInteractions(userRepository, classMembershipRepository, membershipValidator);
    }

    // -------------------------------------------------------------------------
    // Duplicidade dentro da requisição
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deve lançar ClassMembershipException quando a requisição contém o mesmo userId mais de uma vez")
    void shouldThrowWhenRequestHasDuplicateUserIds() {
        UUID duplicatedUserId = UUID.randomUUID();

        BulkAddMembersCommand command = new BulkAddMembersCommand(classId, List.of(
                new BulkAddMembersCommand.Item(duplicatedUserId, ClassRole.STUDENT),
                new BulkAddMembersCommand.Item(duplicatedUserId, ClassRole.STUDENT)
        ));

        when(requestContextProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ClassMembershipException.class);

        verifyNoInteractions(userRepository, classMembershipRepository, membershipValidator);
    }

    // -------------------------------------------------------------------------
    // Falhas de permissão
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deve lançar UserPermissionDeniedException quando executor não tem permissão")
    void shouldThrowWhenExecutorHasNoPermission() {
        UUID userId = UUID.randomUUID();
        RequestContext wegContext = new RequestContext(executorId, TypeUser.WEG, List.of());

        BulkAddMembersCommand command = new BulkAddMembersCommand(classId, List.of(
                new BulkAddMembersCommand.Item(userId, ClassRole.STUDENT)
        ));

        when(requestContextProvider.getRequestContext()).thenReturn(wegContext);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        doThrow(new UserPermissionDeniedException("sem permissão"))
                .when(membershipValidator).validateExecutorCanAddMember(any(), any(), any(), any());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserPermissionDeniedException.class);

        verify(classMembershipRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // Falhas de usuário
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deve lançar UserNotFoundException quando usuário alvo não existe")
    void shouldThrowWhenTargetUserNotFound() {
        UUID userId = UUID.randomUUID();

        BulkAddMembersCommand command = new BulkAddMembersCommand(classId, List.of(
                new BulkAddMembersCommand.Item(userId, ClassRole.STUDENT)
        ));

        when(requestContextProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFoundException.class);

        verify(classMembershipRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve lançar ClassMembershipException quando usuário alvo está inativo")
    void shouldThrowWhenTargetUserIsInactive() {
        UUID userId = UUID.randomUUID();

        BulkAddMembersCommand command = new BulkAddMembersCommand(classId, List.of(
                new BulkAddMembersCommand.Item(userId, ClassRole.STUDENT)
        ));

        when(requestContextProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(userId)).thenReturn(Optional.of(studentA));
        doThrow(new ClassMembershipException("Usuário está inativo ou excluído."))
                .when(membershipValidator).validateTargetUserCanBeAdded(any(), any());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ClassMembershipException.class);

        verify(classMembershipRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // Falhas de vínculo
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("deve lançar ClassMembershipException quando vínculo já existe no banco")
    void shouldThrowWhenMembershipAlreadyExists() {
        UUID userId = UUID.randomUUID();

        BulkAddMembersCommand command = new BulkAddMembersCommand(classId, List.of(
                new BulkAddMembersCommand.Item(userId, ClassRole.STUDENT)
        ));

        when(requestContextProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(userId)).thenReturn(Optional.of(studentA));
        when(classMembershipRepository.existsByUserIdAndClassId(userId, classId)).thenReturn(true);
        doThrow(new ClassMembershipException("O usuário já possui uma matrícula ativa nesta turma."))
                .when(membershipValidator).validateNoDuplicateMembership(true);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ClassMembershipException.class);

        verify(classMembershipRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve lançar ClassMembershipException quando aluno já possui turma ativa")
    void shouldThrowWhenStudentAlreadyHasActiveClass() {
        UUID userId = UUID.randomUUID();

        BulkAddMembersCommand command = new BulkAddMembersCommand(classId, List.of(
                new BulkAddMembersCommand.Item(userId, ClassRole.STUDENT)
        ));

        when(requestContextProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(userId)).thenReturn(Optional.of(studentA));
        when(classMembershipRepository.existsByUserIdAndClassId(userId, classId)).thenReturn(false);
        when(classMembershipRepository.countByUserIdAndClassRole(userId, ClassRole.STUDENT)).thenReturn(1L);
        doThrow(new ClassMembershipException("O aluno já possui uma turma ativa."))
                .when(membershipValidator).validateStudentClassLimit(ClassRole.STUDENT, 1L);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ClassMembershipException.class);

        verify(classMembershipRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // Comportamento transacional
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("não deve persistir nenhum vínculo quando o segundo item falha")
    void shouldNotPersistAnyMembershipWhenSecondItemFails() {
        UUID userIdA = UUID.randomUUID();
        UUID userIdB = UUID.randomUUID();

        BulkAddMembersCommand command = new BulkAddMembersCommand(classId, List.of(
                new BulkAddMembersCommand.Item(userIdA, ClassRole.STUDENT),
                new BulkAddMembersCommand.Item(userIdB, ClassRole.STUDENT)
        ));

        when(requestContextProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findById(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(userIdA)).thenReturn(Optional.of(studentA));
        when(userRepository.findById(userIdB)).thenReturn(Optional.of(studentB));
        when(classMembershipRepository.existsByUserIdAndClassId(userIdA, classId)).thenReturn(false);
        when(classMembershipRepository.existsByUserIdAndClassId(userIdB, classId)).thenReturn(false);
        when(classMembershipRepository.countByUserIdAndClassRole(userIdA, ClassRole.STUDENT)).thenReturn(0L);
        when(classMembershipRepository.countByUserIdAndClassRole(userIdB, ClassRole.STUDENT)).thenReturn(1L);
        when(classMembershipRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        lenient().doThrow(new ClassMembershipException("O aluno já possui uma turma ativa."))
                .when(membershipValidator).validateStudentClassLimit(ClassRole.STUDENT, 1L);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ClassMembershipException.class);

        verify(classMembershipRepository, times(1)).save(any());
    }
}
