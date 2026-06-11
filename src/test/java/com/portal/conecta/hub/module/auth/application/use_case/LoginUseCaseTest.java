package com.portal.conecta.hub.module.auth.application.use_case;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.portal.conecta.hub.module.auth.application.command.LoginCommand;
import com.portal.conecta.hub.module.auth.application.result.LoginResult;
import com.portal.conecta.hub.module.auth.domain.exception.AuthException;
import com.portal.conecta.hub.module.auth.domain.model.AuthUser;
import com.portal.conecta.hub.module.auth.domain.port.RefreshTokenRepository;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private TokenProviderPort tokenProviderPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository repository;

    @Mock
    private ClassMembershipRepository membershipRepository;

    @Mock
    private LoginUseCase useCase;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        useCase = new LoginUseCase(tokenProviderPort, passwordEncoder, repository, membershipRepository, refreshTokenRepository);
    }

    @Test
    void returnsTokensWhenCredentialsAreValid() {
        UUID userId = UUID.randomUUID();
        AuthUser user = user(userId);

        when(repository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(membershipRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
        when(tokenProviderPort.generateAccessToken(eq(user), any())).thenReturn("access-token");
        when(tokenProviderPort.generateRefreshToken(user)).thenReturn("refresh-token");
        when(tokenProviderPort.getAccessTokenExpirationMs()).thenReturn(900_000L);

        LoginResult result = useCase.execute(new LoginCommand("user@test.com", "secret"));

        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());
        assertEquals(900L, result.expiresIn());
    }

    @Test
    void throwsAuthExceptionWhenEmailDoesNotExist() {
        when(repository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(AuthException.class,
                () -> useCase.execute(new LoginCommand("unknown@test.com", "secret")));

        verify(tokenProviderPort, never()).generateAccessToken(any(), any());
    }

    @Test
    void throwsAuthExceptionWhenPasswordIsWrong() {
        UUID userId = UUID.randomUUID();
        AuthUser user = user(userId);

        when(repository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(membershipRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThrows(AuthException.class,
                () -> useCase.execute(new LoginCommand("user@test.com", "wrong")));

        verify(tokenProviderPort, never()).generateAccessToken(any(), any());
    }

    @Test
    void expiresInIsComputedFromConfiguration() {
        UUID userId = UUID.randomUUID();
        AuthUser user = user(userId);

        when(repository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(membershipRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
        when(tokenProviderPort.generateAccessToken(any(), any())).thenReturn("access");
        when(tokenProviderPort.generateRefreshToken(any())).thenReturn("refresh");
        when(tokenProviderPort.getAccessTokenExpirationMs()).thenReturn(1_800_000L);

        LoginResult result = useCase.execute(new LoginCommand("user@test.com", "secret"));

        assertEquals(1800L, result.expiresIn());
    }

    private AuthUser user(UUID id) {
        return new AuthUser() {
            public UUID getId() { return id; }
            public String getPasswordHash() { return "hash"; }
            public TypeUser getType() { return TypeUser.STUDENT; }
            public boolean isActive() { return true; }
        };
    }
}