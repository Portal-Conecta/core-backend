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
import org.springframework.data.jpa.domain.Specification;

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
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(expectedPage);

        Page<UserEntity> result = useCase.execute(new GetAllUserQuery(1, 10, null, null, null, false));

        assertEquals(expectedPage, result);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertPageable(pageableCaptor.getValue(), 1, 10);
    }

    @Test
    void executeListsNotDeletedUsersFilteredByType() {
        Page<UserEntity> expectedPage = new PageImpl<>(List.of());
        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<UserEntity> result = useCase.execute(new GetAllUserQuery(0, 20, TypeUser.STUDENT, null, null, false));

        assertEquals(expectedPage, result);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertPageable(pageableCaptor.getValue(), 0, 20);
    }

    @Test
    void executeUsesPaginationProvidedByQuery() {
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        useCase.execute(new GetAllUserQuery(2, 50, null, null, null, false));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertPageable(pageableCaptor.getValue(), 2, 50);
    }

    @Test
    void queryRejectsNegativePage() {
        InvalidUserDataException exception = assertThrows(
                InvalidUserDataException.class,
                () -> new GetAllUserQuery(-1, 20, null, null, null, false)
        );

    }

    @Test
    void queryRejectsInvalidSize() {
        InvalidUserDataException exception = assertThrows(
                InvalidUserDataException.class,
                () -> new GetAllUserQuery(0, 0, null, null, null, false)
        );

    }

    @Test
    void executeListsActiveUsersFilteredByNameIgnoringCase() {
        Page<UserEntity> expectedPage = new PageImpl<>(List.of());
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(expectedPage);

        Page<UserEntity> result = useCase.execute(new GetAllUserQuery(0, 20, null, "ana", null, false));

        assertEquals(expectedPage, result);
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void executeListsActiveUsersFilteredByTypeAndName() {
        Page<UserEntity> expectedPage = new PageImpl<>(List.of());
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(expectedPage);

        Page<UserEntity> result = useCase.execute(new GetAllUserQuery(0, 20, TypeUser.STUDENT, "ana", null, false));

        assertEquals(expectedPage, result);
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void queryTreatsBlankNameAsNoFilter() {
        GetAllUserQuery query = new GetAllUserQuery(0, 20, null, "  ", null, false);

        assertNull(query.name());
    }

    @Test
    void executeListsUsersForAllRequestedStatuses() {
        Page<UserEntity> expectedPage = new PageImpl<>(List.of());
        List<AccountStatus> statuses = List.of(AccountStatus.PENDING_ACTIVATION, AccountStatus.DISABLED);
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(expectedPage);

        Page<UserEntity> result = useCase.execute(new GetAllUserQuery(0, 20, null, null, statuses, false));

        assertEquals(expectedPage, result);
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }


    private void assertPageable(Pageable pageable, int expectedPage, int expectedSize) {
        assertEquals(expectedPage, pageable.getPageNumber());
        assertEquals(expectedSize, pageable.getPageSize());
        assertEquals(Sort.by("createdAt").descending().and(Sort.by("id").ascending()), pageable.getSort());
    }
}

