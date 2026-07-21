package com.portal.conecta.hub.module.user.application.use_case;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.AccountStatus;
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
    void executeReturnsUserWhenFound() {
        UUID userId = UUID.randomUUID();
        UserEntity expectedUser = new UserEntity("Test User", "test@senai.br", "pass", TypeUser.STUDENT);

        when(userRepository.findByIdAndAccountStatusNot(userId, AccountStatus.PENDING_DELETION))
                .thenReturn(Optional.of(expectedUser));

        UserEntity result = useCase.execute(userId);

        assertEquals(expectedUser, result);
        verify(userRepository).findByIdAndAccountStatusNot(userId, AccountStatus.PENDING_DELETION);
    }

    @Test
    void executeReturnsDisabledUser() {
        UUID userId = UUID.randomUUID();
        UserEntity disabledUser = new UserEntity("Disabled User", "disabled@senai.br", "pass", TypeUser.STUDENT);
        disabledUser.deactivate(disabledUser);

        when(userRepository.findByIdAndAccountStatusNot(userId, AccountStatus.PENDING_DELETION))
                .thenReturn(Optional.of(disabledUser));

        UserEntity result = useCase.execute(userId);

        assertEquals(disabledUser, result);
        assertEquals(AccountStatus.DISABLED, result.getAccountStatus());
        verify(userRepository).findByIdAndAccountStatusNot(userId, AccountStatus.PENDING_DELETION);
    }

    @Test
    void executeThrowsExceptionWhenUserNotFoundOrPendingDeletion() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findByIdAndAccountStatusNot(userId, AccountStatus.PENDING_DELETION))
                .thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> useCase.execute(userId)
        );

        verify(userRepository).findByIdAndAccountStatusNot(userId, AccountStatus.PENDING_DELETION);
    }

    @Test
    void executeThrowsNullPointerExceptionWhenIdIsNull() {
        assertThrows(
                NullPointerException.class,
                () -> useCase.execute(null)
        );
    }
}
