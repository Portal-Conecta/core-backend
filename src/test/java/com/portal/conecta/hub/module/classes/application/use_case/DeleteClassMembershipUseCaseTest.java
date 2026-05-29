package com.portal.conecta.hub.module.classes.application.use_case;

import com.portal.conecta.hub.module.classes.application.command.DeleteMembershipCommand;
import com.portal.conecta.hub.module.classes.domain.exception.ClassMembershipException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteClassMembershipUseCaseTest {

    @Mock private RequestContextProvider requestProvider;
    @Mock private UserRepository userRepository;
    @Mock private ClassMembershipRepository membershipRepository;
    @Mock private ClassMembershipValidator membershipValidator;

    @InjectMocks
    private DeleteClassMembershipUseCase useCase;

    private UUID executorId;
    private UUID targetUserId;
    private UUID classId;
    private UserEntity executor;
    private ClassEntity classEntity;
    private RequestContext adminContext;

    @BeforeEach
    void setUp() {
        executorId = UUID.randomUUID();
        targetUserId = UUID.randomUUID();
        classId = UUID.randomUUID();

        executor = new UserEntity("Executor", "executor@test.com", "hash", TypeUser.ADMIN);
        CourseEntity course = new CourseEntity("Desenvolvimento de Sistemas", "MIDS");
        classEntity = ClassEntity.create(Shift.FULL_AM_PM, 1, course, executor);

        adminContext = new RequestContext(executorId, TypeUser.ADMIN, List.of());
    }

    @Test
    @DisplayName("deve deletar fisicamente o vínculo sem alterar o userType quando classRole for STUDENT")
    void shouldDeleteMembershipPhysicallyForStudent() {
        DeleteMembershipCommand command = new DeleteMembershipCommand(classId, targetUserId);
        UserEntity targetUser = new UserEntity("Student", "student@test.com", "hash", TypeUser.STUDENT);
        ClassMembershipEntity membership = new ClassMembershipEntity(targetUser, classEntity, ClassRole.STUDENT);

        when(requestProvider.getRequestContext()).thenReturn(adminContext);
        when(membershipRepository.findById(new ClassMembershipId(targetUserId, classId)))
                .thenReturn(Optional.of(membership));

        useCase.execute(command);

        verify(membershipValidator).validateExecutorCanDeleteMembership(TypeUser.ADMIN, executorId, targetUserId);
        verify(membershipRepository).delete(membership);
        verifyNoInteractions(userRepository); // Não precisa buscar executor se não for rebaixar
        assertThat(targetUser.getTypeUser()).isEqualTo(TypeUser.STUDENT); // Permanece inalterado
    }

    @Test
    @DisplayName("deve deletar fisicamente o vínculo e rebaixar o usuário para STUDENT quando classRole for REPRESENTATIVE")
    void shouldDeleteMembershipAndDemoteUserWhenRepresentative() {
        DeleteMembershipCommand command = new DeleteMembershipCommand(classId, targetUserId);
        UserEntity targetUser = new UserEntity("Rep", "rep@test.com", "hash", TypeUser.REPRESENTATIVE);
        ClassMembershipEntity membership = new ClassMembershipEntity(targetUser, classEntity, ClassRole.REPRESENTATIVE);

        when(requestProvider.getRequestContext()).thenReturn(adminContext);
        when(membershipRepository.findById(new ClassMembershipId(targetUserId, classId)))
                .thenReturn(Optional.of(membership));
        when(userRepository.findById(executorId)).thenReturn(Optional.of(executor));

        useCase.execute(command);

        verify(membershipRepository).delete(membership);
        assertThat(targetUser.getTypeUser()).isEqualTo(TypeUser.STUDENT); // Foi rebaixado
    }

    @Test
    @DisplayName("deve lançar ClassMembershipNotFoundException (404) quando o vínculo não existir")
    void shouldThrowWhenMembershipNotFound() {
        DeleteMembershipCommand command = new DeleteMembershipCommand(classId, targetUserId);

        when(requestProvider.getRequestContext()).thenReturn(adminContext);
        when(membershipRepository.findById(new ClassMembershipId(targetUserId, classId)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ClassMembershipNotFoundException.class)
                .hasMessageContaining("Membership not found");

        verify(membershipRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deve lançar UserPermissionDeniedException quando executor não tem permissão")
    void shouldThrowWhenExecutorLacksPermission() {
        DeleteMembershipCommand command = new DeleteMembershipCommand(classId, targetUserId);
        RequestContext wegContext = new RequestContext(executorId, TypeUser.WEG, List.of());

        when(requestProvider.getRequestContext()).thenReturn(wegContext);
        doThrow(new UserPermissionDeniedException("sem permissão"))
                .when(membershipValidator).validateExecutorCanDeleteMembership(TypeUser.WEG, executorId, targetUserId);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserPermissionDeniedException.class);

        verifyNoInteractions(membershipRepository);
    }

    @Test
    @DisplayName("deve lançar UserNotFoundException se precisar rebaixar representante e executor não for encontrado")
    void shouldThrowWhenExecutorNotFoundDuringDemotion() {
        DeleteMembershipCommand command = new DeleteMembershipCommand(classId, targetUserId);
        UserEntity targetUser = new UserEntity("Rep", "rep@test.com", "hash", TypeUser.REPRESENTATIVE);
        ClassMembershipEntity membership = new ClassMembershipEntity(targetUser, classEntity, ClassRole.REPRESENTATIVE);

        when(requestProvider.getRequestContext()).thenReturn(adminContext);
        when(membershipRepository.findById(new ClassMembershipId(targetUserId, classId)))
                .thenReturn(Optional.of(membership));
        when(userRepository.findById(executorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UserNotFoundException.class);

        verify(membershipRepository, never()).delete(any());
    }
}