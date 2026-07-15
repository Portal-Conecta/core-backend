package com.portal.conecta.hub.module.user.application.use_case;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.module.user.presentation.dto.response.BulkUserResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GetUsersBulkUseCaseTest {

    @Mock
    private UserRepository userRepository;

    private GetUsersBulkUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetUsersBulkUseCase(userRepository);
    }

    @Test
    void executeReturnsBulkResponseFilteringDuplicatesAndMissingIds() {
        UUID validId1 = UUID.randomUUID();
        UUID validId2 = UUID.randomUUID();
        UUID missingId = UUID.randomUUID();

        List<UUID> requestedIds = List.of(validId1, validId1, validId2, missingId);

        UserEntity user1 = new UserEntity("User One", "one@senai.br", "pass", TypeUser.STUDENT);
        ReflectionTestUtils.setField(user1, "id", validId1);

        UserEntity user2 = new UserEntity("User Two", "two@senai.br", "pass", TypeUser.TEACHER);
        ReflectionTestUtils.setField(user2, "id", validId2);

        when(userRepository.findAllByIdInAndDeletedAtIsNullAndActiveTrue(anyList()))
                .thenReturn(List.of(user1, user2));

        BulkUserResponse response = useCase.execute(requestedIds, false);

        assertEquals(2, response.items().size());
        assertEquals(2, response.foundIds().size());
        assertTrue(response.foundIds().containsAll(List.of(validId1, validId2)));

        assertEquals(1, response.missingIds().size());
        assertTrue(response.missingIds().contains(missingId));

        verify(userRepository).findAllByIdInAndDeletedAtIsNullAndActiveTrue(List.of(validId1, validId2, missingId));
    }

    @Test
    void executeIncludesPendingUsersWhenRequested() {
        UUID activeId = UUID.randomUUID();
        UUID pendingId = UUID.randomUUID();

        UserEntity activeUser = new UserEntity("Active User", "active@senai.br", "pass", TypeUser.STUDENT);
        ReflectionTestUtils.setField(activeUser, "id", activeId);

        UserEntity pendingUser = UserEntity.createPendingActivation(
                "Pending User",
                "pending@estudante.sesisenai.org.br",
                "pass",
                TypeUser.STUDENT,
                null
        );
        ReflectionTestUtils.setField(pendingUser, "id", pendingId);

        when(userRepository.findAllByIdInAndDeletedAtIsNull(List.of(activeId, pendingId)))
                .thenReturn(List.of(activeUser, pendingUser));

        BulkUserResponse response = useCase.execute(List.of(activeId, pendingId), true);

        assertEquals(2, response.items().size());
        assertTrue(response.foundIds().containsAll(List.of(activeId, pendingId)));
        assertTrue(response.missingIds().isEmpty());

        verify(userRepository).findAllByIdInAndDeletedAtIsNull(List.of(activeId, pendingId));
    }
}
