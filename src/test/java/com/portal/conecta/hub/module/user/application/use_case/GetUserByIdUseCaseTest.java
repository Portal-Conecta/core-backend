package com.portal.conecta.hub.module.user.application.use_case;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetUserByIdUseCaseTest {

    @Mock
    private UserRepository userRepository;

    private GetUserByIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetUserByIdUseCase(userRepository);
    }

    @Test
    void executeReturnsUserWhenFoundAndActive() {
        UUID userId = UUID.randomUUID();
        UserEntity expectedUser = new UserEntity("Test User", "test@senai.br", "pass", TypeUser.STUDENT);

        when(userRepository.findByIdAndDeletedAtIsNullAndActiveTrue(userId))
                .thenReturn(Optional.of(expectedUser));

        UserEntity result = useCase.execute(userId);

        assertEquals(expectedUser, result);
        verify(userRepository).findByIdAndDeletedAtIsNullAndActiveTrue(userId);
    }

    @Test
    void executeThrowsExceptionWhenUserNotFoundOrInactive() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findByIdAndDeletedAtIsNullAndActiveTrue(userId))
                .thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> useCase.execute(userId)
        );

        assertEquals("User not found " + userId, exception.getMessage());
        verify(userRepository).findByIdAndDeletedAtIsNullAndActiveTrue(userId);
    }

    @Test
    void executeThrowsNullPointerExceptionWhenIdIsNull() {
        assertThrows(
                NullPointerException.class,
                () -> useCase.execute(null)
        );
    }
}