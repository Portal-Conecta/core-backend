package com.portal.conecta.hub.module.user.application.use_case;

import com.portal.conecta.hub.module.user.application.command.UpdateUserCommand;
import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.module.user.domain.validator.UserPermissionValidator;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateUserUseCaseTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final RequestContextProvider requestContextProvider = mock(RequestContextProvider.class);
    private final UpdateUserUseCase useCase = new UpdateUserUseCase(
            userRepository,
            new UserPermissionValidator(),
            requestContextProvider
    );

    @Test
    void shouldUpdateOnlyNameForAuthorizedRequester() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        UserEntity requester = user("Senai", "senai@sc.senai.br", TypeUser.SENAI, requesterId);
        UserEntity target = user("Student", "student@estudante.sesisenai.org.br", TypeUser.STUDENT, targetId);
        ReflectionTestUtils.setField(target, "avatarUrl", "avatar-original.png");

        when(requestContextProvider.getRequestContext())
                .thenReturn(new RequestContext(requesterId, TypeUser.SENAI, List.of()));
        when(userRepository.findById(targetId)).thenReturn(Optional.of(target));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.save(target)).thenReturn(target);

        UserEntity updated = useCase.execute(new UpdateUserCommand(targetId, " Student Updated "));

        assertEquals("Student Updated", updated.getName());
        assertEquals("student@estudante.sesisenai.org.br", updated.getEmail());
        assertEquals("avatar-original.png", updated.getAvatarUrl());
        assertEquals(TypeUser.STUDENT, updated.getTypeUser());
        verify(userRepository).save(target);
    }

    @Test
    void shouldRejectSelfEditWithoutPersistingChanges() {
        UUID requesterId = UUID.randomUUID();
        UserEntity requester = user("Student", "student@estudante.sesisenai.org.br", TypeUser.STUDENT, requesterId);

        when(requestContextProvider.getRequestContext())
                .thenReturn(new RequestContext(requesterId, TypeUser.STUDENT, List.of()));
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));

        assertThrows(
                UserPermissionDeniedException.class,
                () -> useCase.execute(new UpdateUserCommand(requesterId, "Changed Name"))
        );

        assertEquals("Student", requester.getName());
        verify(userRepository, never()).save(requester);
    }

    private UserEntity user(String name, String email, TypeUser type, UUID id) {
        UserEntity user = new UserEntity(name, email, "hash", type);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
