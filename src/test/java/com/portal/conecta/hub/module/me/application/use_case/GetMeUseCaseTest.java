package com.portal.conecta.hub.module.me.application.use_case;

import com.portal.conecta.hub.module.me.presentation.dto.MyProfileResponse;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.AccountStatus;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetMeUseCaseTest {

    @Mock
    private RequestContextProvider requestContextProvider;

    @Mock
    private UserRepository userRepository;

    private GetMeUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetMeUseCase(requestContextProvider, userRepository);
    }

    @Test
    void shouldReturnAuthenticatedUserProfile() {
        UUID userId = UUID.randomUUID();
        RequestContext context = new RequestContext(userId, TypeUser.STUDENT, List.of());

        UserEntity user = new UserEntity(
                "Lucas Eckert",
                "lucas@senai.br",
                "encoded-password",
                TypeUser.STUDENT
        );
        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(user, "avatarUrl", null);

        when(requestContextProvider.getRequestContext()).thenReturn(context);
        when(userRepository.findByIdAndAccountStatus(userId, AccountStatus.ACTIVE)).thenReturn(Optional.of(user));

        MyProfileResponse response = useCase.execute();

        assertEquals(userId, response.id());
        assertEquals("Lucas Eckert", response.name());
        assertEquals("lucas@senai.br", response.email());
        assertEquals(TypeUser.STUDENT, response.typeUser());
        assertNull(response.avatarUrl());

        verify(userRepository).findByIdAndAccountStatus(userId, AccountStatus.ACTIVE);
    }

    @Test
    void shouldThrowNotFoundWhenAuthenticatedUserDoesNotExistOrIsInactive() {
        UUID userId = UUID.randomUUID();
        RequestContext context = new RequestContext(userId, TypeUser.STUDENT, List.of());

        when(requestContextProvider.getRequestContext()).thenReturn(context);
        when(userRepository.findByIdAndAccountStatus(userId, AccountStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> useCase.execute());
    }
}
