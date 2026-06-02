package com.portal.conecta.hub.module.auth.application.use_case;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.portal.conecta.hub.module.auth.application.command.RefreshTokenCommand;
import com.portal.conecta.hub.module.auth.application.result.RefreshTokenResult;
import com.portal.conecta.hub.module.auth.domain.exception.AuthException;
import com.portal.conecta.hub.module.auth.domain.exception.RefreshTokenException;
import com.portal.conecta.hub.module.auth.domain.model.AuthUser;
import com.portal.conecta.hub.module.auth.domain.port.TokenProviderPort;
import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

    @Mock
    private TokenProviderPort tokenProviderPort;

    @Mock
    private UserRepository repository;

    @Mock
    private ClassMembershipRepository membershipRepository;

    private RefreshTokenUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RefreshTokenUseCase(tokenProviderPort, repository, membershipRepository);
    }

    @Test
    void returnsNewTokensWhenRefreshTokenIsValid() {
        UUID userId = UUID.randomUUID();
        AuthUser user = activeUser(userId);

        when(tokenProviderPort.validateRefreshToken("valid-token")).thenReturn(userId);
        when(repository.findAuthUserById(userId)).thenReturn(Optional.of(user));
        when(membershipRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(tokenProviderPort.generateAccessToken(eq(user), any())).thenReturn("new-access-token");
        when(tokenProviderPort.generateRefreshToken(user)).thenReturn("new-refresh-token");
        when(tokenProviderPort.getAccessTokenExpirationMs()).thenReturn(900_000L);

        RefreshTokenResult result = useCase.execute(new RefreshTokenCommand("valid-token"));

        assertEquals("new-access-token", result.accessToken());
        assertEquals("new-refresh-token", result.refreshToken());
        assertEquals(900L, result.expiresIn());
    }

    @Test
    void throwsAuthExceptionWhenTokenIsExpiredOrInvalid() {
        when(tokenProviderPort.validateRefreshToken("expired-token"))
                .thenThrow(new AuthException("Invalid or expired refresh token"));

        assertThrows(AuthException.class,
                () -> useCase.execute(new RefreshTokenCommand("expired-token")));

        verifyNoInteractions(repository);
    }

    @Test
    void throwsAuthExceptionWhenTokenTypeIsNotRefresh() {
        when(tokenProviderPort.validateRefreshToken("access-token"))
                .thenThrow(new AuthException("Invalid token type"));

        assertThrows(AuthException.class,
                () -> useCase.execute(new RefreshTokenCommand("access-token")));

        verifyNoInteractions(repository);
    }

    @Test
    void throwsRefreshTokenExceptionWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(tokenProviderPort.validateRefreshToken("valid-token")).thenReturn(userId);
        when(repository.findAuthUserById(userId)).thenReturn(Optional.empty());

        assertThrows(RefreshTokenException.class,
                () -> useCase.execute(new RefreshTokenCommand("valid-token")));

        verify(tokenProviderPort, never()).generateAccessToken(any(), any());
    }

    @Test
    void throwsRefreshTokenExceptionWhenUserIsInactive() {
        UUID userId = UUID.randomUUID();
        AuthUser user = Mockito.mock(AuthUser.class);
        when(user.isActive()).thenReturn(false);

        when(tokenProviderPort.validateRefreshToken("valid-token")).thenReturn(userId);
        when(repository.findAuthUserById(any())).thenReturn(Optional.of(user));

        Exception ex = assertThrows(Exception.class,
                () -> useCase.execute(new RefreshTokenCommand("valid-token")));

        System.out.println(ex.getClass().getName() + ": " + ex.getMessage());

        assertEquals("User is inactive or blocked", ex.getMessage());
        verify(tokenProviderPort, never()).generateAccessToken(any(), any());
    }

    @Test
    void fetchesCurrentMembershipsBeforeGeneratingAccessToken() {
        UUID userId = UUID.randomUUID();
        AuthUser user = activeUser(userId);

        when(tokenProviderPort.validateRefreshToken("valid-token")).thenReturn(userId);
        when(repository.findAuthUserById(userId)).thenReturn(Optional.of(user));
        when(membershipRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(tokenProviderPort.generateAccessToken(any(), any())).thenReturn("access");
        when(tokenProviderPort.generateRefreshToken(any())).thenReturn("refresh");
        when(tokenProviderPort.getAccessTokenExpirationMs()).thenReturn(900_000L);

        useCase.execute(new RefreshTokenCommand("valid-token"));

        verify(membershipRepository).findAllByUserId(userId);
    }

    private AuthUser activeUser(UUID id) {
        return new AuthUser() {
            public UUID getId() { return id; }
            public String getPasswordHash() { return "hash"; }
            public TypeUser getType() { return TypeUser.STUDENT; }
            public boolean isActive() { return true; }
        };
    }

    private AuthUser inactiveUser(UUID id) {
        return new AuthUser() {
            public UUID getId() { return id; }
            public String getPasswordHash() { return "hash"; }
            public TypeUser getType() { return TypeUser.STUDENT; }
            public boolean isActive() { return false; }
        };
    }
}