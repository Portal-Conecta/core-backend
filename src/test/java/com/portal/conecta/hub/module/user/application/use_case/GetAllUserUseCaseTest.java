package com.portal.conecta.hub.module.user.application.use_case;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.portal.conecta.hub.module.user.application.query.GetAllUserQuery;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
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
        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.ADMIN, List.of()));
        Page<UserEntity> expectedPage = new PageImpl<>(List.of());
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(expectedPage);

        Page<UserEntity> result = useCase.execute(new GetAllUserQuery(1, 10, null, null, null));

        assertEquals(expectedPage, result);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertPageable(pageableCaptor.getValue(), 1, 10);
    }

    @Test
    void executeListsNotDeletedUsersFilteredByType() {
        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.SENAI, List.of()));
        Page<UserEntity> expectedPage = new PageImpl<>(List.of());
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(expectedPage);

        Page<UserEntity> result = useCase.execute(new GetAllUserQuery(0, 20, TypeUser.STUDENT, null, null));

        assertEquals(expectedPage, result);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertPageable(pageableCaptor.getValue(), 0, 20);
    }

    @Test
    void executeUsesPaginationProvidedByQuery() {
        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.WEG, List.of()));
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        useCase.execute(new GetAllUserQuery(2, 50, null, null, null));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertPageable(pageableCaptor.getValue(), 2, 50);
    }

    @Test
    void executeAcceptsNameAndExcludedClassFilters() {
        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.ADMIN, List.of()));
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        UUID excludeClassId = UUID.randomUUID();

        useCase.execute(new GetAllUserQuery(0, 20, TypeUser.STUDENT, "ana", excludeClassId));

        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void queryRejectsNegativePage() {
        InvalidUserDataException exception = assertThrows(
                InvalidUserDataException.class,
                () -> new GetAllUserQuery(-1, 20, null, null, null)
        );

    }

    @Test
    void queryRejectsInvalidSize() {
        InvalidUserDataException exception = assertThrows(
                InvalidUserDataException.class,
                () -> new GetAllUserQuery(0, 0, null, null, null)
        );

    }

    @Test
    void executeRejectsUserWithoutPermissionBeforeQueryingRepository() {
        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.STUDENT, List.of()));

        assertThrows(
                UserPermissionDeniedException.class,
                () -> useCase.execute(new GetAllUserQuery(0, 20, null, null, null))
        );

        verify(userRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    private void assertPageable(Pageable pageable, int expectedPage, int expectedSize) {
        assertEquals(expectedPage, pageable.getPageNumber());
        assertEquals(expectedSize, pageable.getPageSize());
        assertEquals(Sort.by("createdAt").descending().and(Sort.by("id").ascending()), pageable.getSort());
    }
}
