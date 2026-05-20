package com.portal.conecta.hub.module.user.application.use_case;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.portal.conecta.hub.module.user.application.query.GetAllUserQuery;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
        when(userRepository.findByDeletedAtIsNull(any(Pageable.class))).thenReturn(expectedPage);

        Page<UserEntity> result = useCase.execute(new GetAllUserQuery(1, 10, null));

        assertEquals(expectedPage, result);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findByDeletedAtIsNull(pageableCaptor.capture());
        assertEquals(PageRequest.of(1, 10), pageableCaptor.getValue());
        verify(userRepository, never()).findByDeletedAtIsNullAndType(any(TypeUser.class), any(Pageable.class));
    }

    @Test
    void executeListsNotDeletedUsersFilteredByType() {
        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.SENAI, List.of()));
        Page<UserEntity> expectedPage = new PageImpl<>(List.of());
        when(userRepository.findByDeletedAtIsNullAndType(any(TypeUser.class), any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<UserEntity> result = useCase.execute(new GetAllUserQuery(0, 20, TypeUser.STUDENT));

        assertEquals(expectedPage, result);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findByDeletedAtIsNullAndType(org.mockito.ArgumentMatchers.eq(TypeUser.STUDENT), pageableCaptor.capture());
        assertEquals(PageRequest.of(0, 20), pageableCaptor.getValue());
        verify(userRepository, never()).findByDeletedAtIsNull(any(Pageable.class));
    }

    @Test
    void executeUsesPaginationProvidedByQuery() {
        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.WEG, List.of()));
        when(userRepository.findByDeletedAtIsNull(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        useCase.execute(new GetAllUserQuery(2, 50, null));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findByDeletedAtIsNull(pageableCaptor.capture());
        assertEquals(PageRequest.of(2, 50), pageableCaptor.getValue());
    }

    @Test
    void executeRejectsUserWithoutPermissionBeforeQueryingRepository() {
        when(contextProvider.getRequestContext())
                .thenReturn(new RequestContext(UUID.randomUUID(), TypeUser.STUDENT, List.of()));

        assertThrows(
                UserPermissionDeniedException.class,
                () -> useCase.execute(new GetAllUserQuery(0, 20, null))
        );

        verify(userRepository, never()).findByDeletedAtIsNull(any(Pageable.class));
        verify(userRepository, never()).findByDeletedAtIsNullAndType(any(TypeUser.class), any(Pageable.class));
    }
}
