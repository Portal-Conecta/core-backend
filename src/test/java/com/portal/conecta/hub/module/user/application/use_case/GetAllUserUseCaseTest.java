package com.portal.conecta.hub.module.user.application.use_case;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.portal.conecta.hub.module.user.application.query.GetAllUserQuery;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.AccountStatus;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.module.user.domain.validator.UserPermissionValidator;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class GetAllUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RequestContextProvider contextProvider;

    private GetAllUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetAllUserUseCase(
                userRepository,
                new UserPermissionValidator(),
                contextProvider
        );
    }

    @Test
    void executeListsNotDeletedUsersForAuthorizedUser() {
        Page<UserEntity> expectedPage = new PageImpl<>(List.of());
        when(userRepository.findByAccountStatus(eq(AccountStatus.ACTIVE), any(Pageable.class))).thenReturn(expectedPage);

        Page<UserEntity> result = useCase.execute(new GetAllUserQuery(1, 10, null, null));

        assertEquals(expectedPage, result);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findByAccountStatus(eq(AccountStatus.ACTIVE), pageableCaptor.capture());
        assertPageable(pageableCaptor.getValue(), 1, 10);
        verify(userRepository, never()).findByAccountStatusAndType(eq(AccountStatus.ACTIVE), any(TypeUser.class), any(Pageable.class));
    }

    @Test
    void executeListsNotDeletedUsersFilteredByType() {
        Page<UserEntity> expectedPage = new PageImpl<>(List.of());
        when(userRepository.findByAccountStatusAndType(eq(AccountStatus.ACTIVE), any(TypeUser.class), any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<UserEntity> result = useCase.execute(new GetAllUserQuery(0, 20, TypeUser.STUDENT, null));

        assertEquals(expectedPage, result);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findByAccountStatusAndType(eq(AccountStatus.ACTIVE), eq(TypeUser.STUDENT), pageableCaptor.capture());
        assertPageable(pageableCaptor.getValue(), 0, 20);
        verify(userRepository, never()).findByAccountStatus(eq(AccountStatus.ACTIVE), any(Pageable.class));
    }

    @Test
    void executeUsesPaginationProvidedByQuery() {
        when(userRepository.findByAccountStatus(eq(AccountStatus.ACTIVE), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        useCase.execute(new GetAllUserQuery(2, 50, null, null));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findByAccountStatus(eq(AccountStatus.ACTIVE), pageableCaptor.capture());
        assertPageable(pageableCaptor.getValue(), 2, 50);
    }

    @Test
    void queryRejectsNegativePage() {
        InvalidUserDataException exception = assertThrows(
                InvalidUserDataException.class,
                () -> new GetAllUserQuery(-1, 20, null, null)
        );

    }

    @Test
    void queryRejectsInvalidSize() {
        InvalidUserDataException exception = assertThrows(
                InvalidUserDataException.class,
                () -> new GetAllUserQuery(0, 0, null, null)
        );

    }

    @Test
    void executeListsActiveUsersFilteredByNameIgnoringCase() {
        Page<UserEntity> expectedPage = new PageImpl<>(List.of());
        when(userRepository.findByAccountStatusAndNameContainingIgnoreCase(
                eq(AccountStatus.ACTIVE), eq("ana"), any(Pageable.class)
        )).thenReturn(expectedPage);

        Page<UserEntity> result = useCase.execute(new GetAllUserQuery(0, 20, null, "ana"));

        assertEquals(expectedPage, result);
        verify(userRepository).findByAccountStatusAndNameContainingIgnoreCase(
                eq(AccountStatus.ACTIVE), eq("ana"), any(Pageable.class)
        );
        verify(userRepository, never()).findByAccountStatus(eq(AccountStatus.ACTIVE), any(Pageable.class));
        verify(userRepository, never()).findByAccountStatusAndType(
                eq(AccountStatus.ACTIVE), any(TypeUser.class), any(Pageable.class)
        );
    }

    @Test
    void executeListsActiveUsersFilteredByTypeAndName() {
        Page<UserEntity> expectedPage = new PageImpl<>(List.of());
        when(userRepository.findByAccountStatusAndTypeAndNameContainingIgnoreCase(
                eq(AccountStatus.ACTIVE), eq(TypeUser.STUDENT), eq("ana"), any(Pageable.class)
        )).thenReturn(expectedPage);

        Page<UserEntity> result = useCase.execute(new GetAllUserQuery(0, 20, TypeUser.STUDENT, "ana"));

        assertEquals(expectedPage, result);
        verify(userRepository).findByAccountStatusAndTypeAndNameContainingIgnoreCase(
                eq(AccountStatus.ACTIVE), eq(TypeUser.STUDENT), eq("ana"), any(Pageable.class)
        );
    }

    @Test
    void queryTreatsBlankNameAsNoFilter() {
        GetAllUserQuery query = new GetAllUserQuery(0, 20, null, "  ");

        assertNull(query.name());
    }


    private void assertPageable(Pageable pageable, int expectedPage, int expectedSize) {
        assertEquals(expectedPage, pageable.getPageNumber());
        assertEquals(expectedSize, pageable.getPageSize());
        assertEquals(Sort.by("createdAt").descending().and(Sort.by("id").ascending()), pageable.getSort());
    }
}

