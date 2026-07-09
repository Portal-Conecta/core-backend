package com.portal.conecta.hub.module.classes.application.use_case;

import com.portal.conecta.hub.module.classes.application.command.DemoteMemberCommand;
import com.portal.conecta.hub.module.classes.application.use_case.membership.DemoteFromRepresentativeUseCase;
import com.portal.conecta.hub.module.classes.domain.exception.ClassMembershipNotFoundException;
import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipId;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.classes.domain.model.Shift;
import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
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
class DemoteFromRepresentativeUseCaseTest {

    @Mock private RequestContextProvider requestProvider;
    @Mock private UserRepository userRepository;
    @Mock private ClassMembershipRepository membershipRepository;
    @Mock private ClassMembershipValidator membershipValidator;

    @InjectMocks
    private DemoteFromRepresentativeUseCase useCase;

    private UUID executorId;
    private UUID targetUserId;
    private UUID classId;
    private UserEntity executor;
    private UserEntity targetRepresentative;
    private ClassMembershipEntity representativeMembership;
    private RequestContext adminContext;

    @BeforeEach
    void setUp() {
        executorId = UUID.randomUUID();
        targetUserId = UUID.randomUUID();
        classId = UUID.randomUUID();

        executor = new UserEntity("Executor", "executor@test.com", "hash", TypeUser.ADMIN);
        targetRepresentative = new UserEntity("Rep", "rep@test.com", "hash", TypeUser.REPRESENTATIVE);

        CourseEntity course = new CourseEntity("Desenvolvimento de Sistemas", "MIDS");
        ClassEntity classEntity = ClassEntity.create(Shift.FULL_AM_PM, 1, course, executor);
        representativeMembership = new ClassMembershipEntity(targetRepresentative, classEntity, ClassRole.REPRESENTATIVE);

        adminContext = new RequestContext(executorId, TypeUser.ADMIN, List.of());
    }

    // --- Sucesso ---

    @Test
    @DisplayName("ADMIN remove representante de turma com sucesso, alterando papéis para STUDENT")
    void shouldDemoteRepresentativeSuccessfullyAsAdmin() {
        DemoteMemberCommand command = new DemoteMemberCommand(classId, targetUserId);

        when(requestProvider.getRequestContext()).thenReturn(adminContext);
        when(membershipRepository.findById(new ClassMembershipId(targetUserId, classId)))
                .thenReturn(Optional.of(representativeMembership));
        when(userRepository.findById(executorId)).thenReturn(Optional.of(executor));
        when(membershipRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ClassMembershipEntity result = useCase.execute(command);

        assertThat(result.getClassRole()).isEqualTo(ClassRole.STUDENT);
        assertThat(result.getUser().getTypeUser()).isEqualTo(TypeUser.STUDENT);
        verify(membershipValidator).validateExecutorCanDemote(TypeUser.ADMIN);
        verify(membershipValidator).validateTargetUserForDemotion(representativeMembership);
        verify(membershipRepository).save(representativeMembership);
    }

    // --- Falhas ---

    @Test
    @DisplayName("deve lançar UserPermissionDeniedException quando executor não tem permissão (ex: WEG)")
    void shouldThrowWhenExecutorLacksPermission() {
        DemoteMemberCommand command = new DemoteMemberCommand(classId, targetUserId);
        RequestContext wegContext = new RequestContext(executorId, TypeUser.WEG, List.of());

        when(requestProvider.getRequestContext()).thenReturn(wegContext);
        doThrow(new UserPermissionDeniedException("sem permissão"))
                .when(membershipValidator).validateExecutorCanDemote(TypeUser.WEG);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserPermissionDeniedException.class);

        verifyNoInteractions(membershipRepository, userRepository);
    }

    @Test
    @DisplayName("deve lançar ClassMembershipNotFound (404) quando o vínculo não existe")
    void shouldThrowWhenMembershipNotFound() {
        DemoteMemberCommand command = new DemoteMemberCommand(classId, targetUserId);

        when(requestProvider.getRequestContext()).thenReturn(adminContext);
        when(membershipRepository.findById(new ClassMembershipId(targetUserId, classId)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ClassMembershipNotFoundException.class);

        verify(membershipRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve lançar UserNotFoundException quando executor não é encontrado na base")
    void shouldThrowWhenExecutorNotFound() {
        DemoteMemberCommand command = new DemoteMemberCommand(classId, targetUserId);

        when(requestProvider.getRequestContext()).thenReturn(adminContext);
        when(membershipRepository.findById(new ClassMembershipId(targetUserId, classId)))
                .thenReturn(Optional.of(representativeMembership));
        when(userRepository.findById(executorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFoundException.class);

        verify(membershipRepository, never()).save(any());
    }
}