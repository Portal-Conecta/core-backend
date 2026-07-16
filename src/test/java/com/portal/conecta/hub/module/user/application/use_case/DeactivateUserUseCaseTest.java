package com.portal.conecta.hub.module.user.application.use_case;

import com.portal.conecta.hub.module.user.application.command.DeactivateUserCommand;
import com.portal.conecta.hub.module.user.domain.exception.UserAlreadyInactiveException;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.AccountStatus;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.module.user.domain.validator.UserPermissionValidator;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeactivateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPermissionValidator permissionValidator;

    @Mock
    private RequestContextProvider contextProvider;

    private DeactivateUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeactivateUserUseCase(userRepository, permissionValidator, contextProvider);
    }

    @Test
    void shouldDisableActiveUserWithoutDeletedAt() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UserEntity requester = new UserEntity("Admin", "admin@senai.br", "hash", TypeUser.ADMIN);
        UserEntity activeUser = new UserEntity("Student", "student@estudante.sesisenai.org.br", "hash", TypeUser.STUDENT);
        ReflectionTestUtils.setField(requester, "id", requesterId);
        ReflectionTestUtils.setField(activeUser, "id", targetId);

        when(contextProvider.getRequestContext()).thenReturn(new RequestContext(requesterId, TypeUser.ADMIN, List.of()));
        when(userRepository.findById(targetId)).thenReturn(Optional.of(activeUser));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.save(activeUser)).thenReturn(activeUser);

        UserEntity result = useCase.execute(new DeactivateUserCommand(targetId));

        assertThat(result.getAccountStatus()).isEqualTo(AccountStatus.DISABLED);
        assertThat(result.getDeletedAt()).isNull();
        assertThat(result.isRemoved()).isFalse();
        verify(permissionValidator).validateCanDeactivate(TypeUser.ADMIN, TypeUser.STUDENT);
        verify(userRepository).save(activeUser);
    }

    @Test
    void shouldThrowWhenUserIsAlreadyDisabled() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UserEntity requester = new UserEntity("Admin", "admin@senai.br", "hash", TypeUser.ADMIN);
        UserEntity disabledUser = new UserEntity("Disabled Student", "disabled@estudante.sesisenai.org.br", "hash", TypeUser.STUDENT);
        disabledUser.deactivate(requester);
        ReflectionTestUtils.setField(disabledUser, "id", targetId);

        when(contextProvider.getRequestContext()).thenReturn(new RequestContext(requesterId, TypeUser.ADMIN, List.of()));
        when(userRepository.findById(targetId)).thenReturn(Optional.of(disabledUser));

        assertThatThrownBy(() -> useCase.execute(new DeactivateUserCommand(targetId)))
                .isInstanceOf(UserAlreadyInactiveException.class);

        verify(permissionValidator, never()).validateCanDeactivate(TypeUser.ADMIN, TypeUser.STUDENT);
        verify(userRepository, never()).save(disabledUser);
    }

    @Test
    void shouldHideUserPendingDeletion() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UserEntity requester = new UserEntity("Admin", "admin@senai.br", "hash", TypeUser.ADMIN);
        UserEntity removedUser = new UserEntity("Removed Student", "removed@estudante.sesisenai.org.br", "hash", TypeUser.STUDENT);
        removedUser.delete(requester);
        ReflectionTestUtils.setField(removedUser, "id", targetId);

        when(contextProvider.getRequestContext()).thenReturn(new RequestContext(requesterId, TypeUser.ADMIN, List.of()));
        when(userRepository.findById(targetId)).thenReturn(Optional.of(removedUser));

        assertThatThrownBy(() -> useCase.execute(new DeactivateUserCommand(targetId)))
                .isInstanceOf(UserNotFoundException.class);

        verify(permissionValidator, never()).validateCanDeactivate(TypeUser.ADMIN, TypeUser.STUDENT);
        verify(userRepository, never()).save(removedUser);
    }
}
