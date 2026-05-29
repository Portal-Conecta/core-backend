package com.portal.conecta.hub.module.classes.application.use_case;

import com.portal.conecta.hub.module.classes.application.command.PromoteMemberCommand;
import com.portal.conecta.hub.module.classes.domain.exception.ClassMembershipException;
import com.portal.conecta.hub.module.classes.domain.exception.ClassEntityNotFoundException;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipId;
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
class PromoteToRepresentativeUseCaseTest {

    @Mock private RequestContextProvider requestProvider;
    @Mock private ClassRepository classRepository;
    @Mock private UserRepository userRepository;
    @Mock private ClassMembershipRepository membershipRepository;
    @Mock private ClassMembershipValidator membershipValidator;

    @InjectMocks
    private PromoteToRepresentativeUseCase useCase;

    private UUID executorId;
    private UUID targetUserId;
    private UUID classId;
    private UserEntity executor;
    private UserEntity targetStudent;
    private ClassEntity classEntity;
    private ClassMembershipEntity studentMembership;
    private RequestContext senaiContext;

    @BeforeEach
    void setUp() {
        executorId = UUID.randomUUID();
        targetUserId = UUID.randomUUID();
        classId = UUID.randomUUID();

        executor = new UserEntity("Executor", "executor@test.com", "hash", TypeUser.SENAI);
        targetStudent = new UserEntity("Student", "student@test.com", "hash", TypeUser.STUDENT);

        CourseEntity course = new CourseEntity("Desenvolvimento de Sistemas", "MIDS");
        classEntity = ClassEntity.create(Shift.FULL_AM_PM, 1, course, executor);
        studentMembership = new ClassMembershipEntity(targetStudent, classEntity, ClassRole.STUDENT);

        senaiContext = new RequestContext(executorId, TypeUser.SENAI, List.of());
    }

    // --- sucesso ---

    @Test
    @DisplayName("SENAI promove aluno vinculado para representante com sucesso")
    void shouldPromoteStudentSuccessfullyAsSenai() {
        PromoteMemberCommand command = new PromoteMemberCommand(classId, targetUserId);

        when(requestProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findByIdForUpdate(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetStudent));
        when(membershipRepository.findById(new ClassMembershipId(targetUserId, classId)))
                .thenReturn(Optional.of(studentMembership));
        when(membershipRepository.countByClassIdAndClassRole(classId, ClassRole.REPRESENTATIVE)).thenReturn(0L);
        when(userRepository.findById(executorId)).thenReturn(Optional.of(executor));
        when(membershipRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ClassMembershipEntity result = useCase.execute(command);

        assertThat(result.getClassRole()).isEqualTo(ClassRole.REPRESENTATIVE);
        assertThat(result.getUser().getTypeUser()).isEqualTo(TypeUser.REPRESENTATIVE);
        verify(membershipRepository).save(studentMembership);
    }

    @Test
    @DisplayName("ADMIN promove aluno vinculado para representante com sucesso")
    void shouldPromoteStudentSuccessfullyAsAdmin() {
        PromoteMemberCommand command = new PromoteMemberCommand(classId, targetUserId);
        RequestContext adminContext = new RequestContext(executorId, TypeUser.ADMIN, List.of());

        when(requestProvider.getRequestContext()).thenReturn(adminContext);
        when(classRepository.findByIdForUpdate(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetStudent));
        when(membershipRepository.findById(new ClassMembershipId(targetUserId, classId)))
                .thenReturn(Optional.of(studentMembership));
        when(membershipRepository.countByClassIdAndClassRole(classId, ClassRole.REPRESENTATIVE)).thenReturn(1L);
        when(userRepository.findById(executorId)).thenReturn(Optional.of(executor));
        when(membershipRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ClassMembershipEntity result = useCase.execute(command);

        assertThat(result.getClassRole()).isEqualTo(ClassRole.REPRESENTATIVE);
        verify(membershipRepository).save(any());
    }

    // --- falhas de permissão ---

    @Test
    @DisplayName("deve lançar UserPermissionDeniedException quando executor é WEG")
    void shouldThrowWhenExecutorIsWeg() {
        PromoteMemberCommand command = new PromoteMemberCommand(classId, targetUserId);
        RequestContext wegContext = new RequestContext(executorId, TypeUser.WEG, List.of());

        when(requestProvider.getRequestContext()).thenReturn(wegContext);
        doThrow(new UserPermissionDeniedException("sem permissão"))
                .when(membershipValidator).validateExecutorCanPromote(TypeUser.WEG);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserPermissionDeniedException.class);

        verifyNoInteractions(classRepository, userRepository, membershipRepository);
    }

    // --- falhas de turma ---

    @Test
    @DisplayName("deve lançar ClassNotFoundException quando turma não existe")
    void shouldThrowWhenClassNotFound() {
        PromoteMemberCommand command = new PromoteMemberCommand(classId, targetUserId);

        when(requestProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findByIdForUpdate(classId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ClassEntityNotFoundException.class)
                .hasMessageContaining(classId.toString());

        verifyNoInteractions(userRepository, membershipRepository);
    }

    @Test
    @DisplayName("deve lançar ClassMembershipException quando turma está deletada")
    void shouldThrowWhenClassIsDeleted() {
        PromoteMemberCommand command = new PromoteMemberCommand(classId, targetUserId);

        classEntity.delete(executor);

        when(requestProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findByIdForUpdate(classId)).thenReturn(Optional.of(classEntity));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ClassMembershipException.class);

        verifyNoInteractions(userRepository, membershipRepository);
    }

    // --- falhas de usuário ---

    @Test
    @DisplayName("deve lançar UserNotFoundException quando usuário não existe")
    void shouldThrowWhenUserNotFound() {
        PromoteMemberCommand command = new PromoteMemberCommand(classId, targetUserId);

        when(requestProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findByIdForUpdate(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(targetUserId.toString());

        verifyNoInteractions(membershipRepository);
    }

    // --- falhas de vínculo e elegibilidade ---

    @Test
    @DisplayName("deve lançar ClassMembershipException quando usuário não tem vínculo com a turma")
    void shouldThrowWhenMembershipNotFound() {
        PromoteMemberCommand command = new PromoteMemberCommand(classId, targetUserId);

        when(requestProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findByIdForUpdate(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetStudent));
        when(membershipRepository.findById(new ClassMembershipId(targetUserId, classId)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ClassMembershipException.class);

        verify(membershipRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve lançar ClassMembershipException quando vínculo não é STUDENT")
    void shouldThrowWhenMembershipRoleIsNotStudent() {
        PromoteMemberCommand command = new PromoteMemberCommand(classId, targetUserId);
        UserEntity teacher = new UserEntity("Teacher", "teacher@test.com", "hash", TypeUser.TEACHER);
        ClassMembershipEntity teacherMembership = new ClassMembershipEntity(teacher, classEntity, ClassRole.TEACHER);

        when(requestProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findByIdForUpdate(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(teacher));
        when(membershipRepository.findById(new ClassMembershipId(targetUserId, classId)))
                .thenReturn(Optional.of(teacherMembership));
        doThrow(new ClassMembershipException("classRole não é STUDENT"))
                .when(membershipValidator).validateTargetUserForPromotion(any(), any());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ClassMembershipException.class);

        verify(membershipRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve lançar ClassMembershipException quando turma já tem dois representantes")
    void shouldThrowWhenRepresentativeLimitReached() {
        PromoteMemberCommand command = new PromoteMemberCommand(classId, targetUserId);

        when(requestProvider.getRequestContext()).thenReturn(senaiContext);
        when(classRepository.findByIdForUpdate(classId)).thenReturn(Optional.of(classEntity));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetStudent));
        when(membershipRepository.findById(new ClassMembershipId(targetUserId, classId)))
                .thenReturn(Optional.of(studentMembership));
        when(membershipRepository.countByClassIdAndClassRole(classId, ClassRole.REPRESENTATIVE)).thenReturn(2L);
        doThrow(new ClassMembershipException("limite de representantes atingido"))
                .when(membershipValidator).validateRepresentativeSlotAvailable(2L);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ClassMembershipException.class);

        verify(membershipRepository, never()).save(any());
    }
}