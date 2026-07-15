package com.portal.conecta.hub.module.user.application.use_case;

import com.portal.conecta.hub.module.user.application.command.DeactivateUserCommand;
import com.portal.conecta.hub.module.user.domain.exception.UserAlreadyInactiveException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.module.user.domain.validator.UserPermissionValidator;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    @DisplayName("deve permitir remover usuÃ¡rio pendente de ativaÃ§Ã£o")
    void shouldDeactivatePendingActivationUser() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UserEntity requester = new UserEntity("Admin", "admin@senai.br", "hash", TypeUser.ADMIN);
        UserEntity pendingUser = UserEntity.createPendingActivation(
                "Pending Student",
                "pending@estudante.sesisenai.org.br",
                "hash",
                TypeUser.STUDENT,
                requester
        );
        ReflectionTestUtils.setField(requester, "id", requesterId);
        ReflectionTestUtils.setField(pendingUser, "id", targetId);

        when(contextProvider.getRequestContext()).thenReturn(new RequestContext(requesterId, TypeUser.ADMIN, List.of()));
        when(userRepository.findById(targetId)).thenReturn(Optional.of(pendingUser));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));

        useCase.execute(new DeactivateUserCommand(targetId));

        assertThat(pendingUser.isRemoved()).isTrue();
        verify(permissionValidator).validateCanDeactivate(TypeUser.ADMIN, TypeUser.STUDENT);
        verify(userRepository).save(pendingUser);
    }

    @Test
    @DisplayName("deve bloquear usuÃ¡rio jÃ¡ removido")
    void shouldThrowWhenUserIsAlreadyRemoved() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UserEntity requester = new UserEntity("Admin", "admin@senai.br", "hash", TypeUser.ADMIN);
        UserEntity removedUser = new UserEntity("Removed Student", "removed@estudante.sesisenai.org.br", "hash", TypeUser.STUDENT);
        removedUser.delete(requester);
        ReflectionTestUtils.setField(removedUser, "id", targetId);

        when(contextProvider.getRequestContext()).thenReturn(new RequestContext(requesterId, TypeUser.ADMIN, List.of()));
        when(userRepository.findById(targetId)).thenReturn(Optional.of(removedUser));

        assertThatThrownBy(() -> useCase.execute(new DeactivateUserCommand(targetId)))
                .isInstanceOf(UserAlreadyInactiveException.class);

        verify(permissionValidator, never()).validateCanDeactivate(TypeUser.ADMIN, TypeUser.STUDENT);
        verify(userRepository, never()).save(removedUser);
    }
}
