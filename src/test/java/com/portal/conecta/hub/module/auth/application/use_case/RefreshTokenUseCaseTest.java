package com.portal.conecta.hub.module.auth.application.use_case;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.portal.conecta.hub.module.auth.application.command.RefreshTokenCommand;
import com.portal.conecta.hub.module.auth.application.result.RefreshTokenResult;
import com.portal.conecta.hub.module.auth.domain.exception.AuthException;
import com.portal.conecta.hub.module.auth.domain.exception.InvalidRefreshTokenException;
import com.portal.conecta.hub.module.auth.domain.exception.RefreshTokenException;
import com.portal.conecta.hub.module.auth.domain.model.AuthUser;
import com.portal.conecta.hub.module.auth.domain.model.RefreshTokenEntity;
import com.portal.conecta.hub.module.auth.domain.port.RefreshTokenRepository;
import com.portal.conecta.hub.module.auth.domain.port.TokenProviderPort;
import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class RefreshTokenUseCaseTest {

    @Mock
    private TokenProviderPort tokenProviderPort;

    @Mock
    private UserRepository repository;

    @Mock
    private ClassMembershipRepository membershipRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenUseCase useCase;

    @BeforeEach
    void setUp() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger("com.portal.conecta.hub.module.auth.application.use_case").setLevel(Level.INFO);

        useCase = new RefreshTokenUseCase(tokenProviderPort, repository, membershipRepository, refreshTokenRepository);
    }

    @Test
    void returnsNewTokensWhenRefreshTokenIsValid(CapturedOutput output) {
        UUID userId = UUID.randomUUID();
        AuthUser user = activeUser(userId);
        RefreshTokenEntity existingToken = new RefreshTokenEntity("valid-token", Instant.now().plusSeconds(3600));

        when(tokenProviderPort.validateRefreshToken("valid-token")).thenReturn(userId);
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(existingToken));
        when(repository.findAuthUserById(userId)).thenReturn(Optional.of(user));
        when(membershipRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(tokenProviderPort.generateAccessToken(eq(user), any())).thenReturn("new-access-token");
        when(tokenProviderPort.generateRefreshToken(user)).thenReturn("new-refresh-token");
        when(tokenProviderPort.getAccessTokenExpirationMs()).thenReturn(900_000L);
        when(tokenProviderPort.getRefreshTokenExpirationMs()).thenReturn(604_800_000L);

        RefreshTokenResult result = useCase.execute(new RefreshTokenCommand("valid-token"));

        assertEquals("new-access-token", result.accessToken());
        assertEquals("new-refresh-token", result.refreshToken());
        assertEquals(900L, result.expiresIn());

        assertThat(output).contains("renovado com sucesso");
        assertNoSensitiveData(output);
    }

    @Test
    void throwsAuthExceptionWhenTokenIsExpiredOrInvalid(CapturedOutput output) {
        when(tokenProviderPort.validateRefreshToken("expired-token"))
                .thenThrow(new AuthException("Invalid or expired refresh token"));

        assertThrows(AuthException.class,
                () -> useCase.execute(new RefreshTokenCommand("expired-token")));

        verifyNoInteractions(repository);
        assertNoBusinessLog(output);
        assertNoSensitiveData(output);
    }

    @Test
    void throwsAuthExceptionWhenTokenTypeIsNotRefresh(CapturedOutput output) {
        when(tokenProviderPort.validateRefreshToken("access-token"))
                .thenThrow(new AuthException("Invalid token type"));

        assertThrows(AuthException.class,
                () -> useCase.execute(new RefreshTokenCommand("access-token")));

        verifyNoInteractions(repository);
        assertNoBusinessLog(output);
        assertNoSensitiveData(output);
    }

    @Test
    void throwsRefreshTokenExceptionWhenTokenNotFoundInDatabase(CapturedOutput output) {
        UUID userId = UUID.randomUUID();

        when(tokenProviderPort.validateRefreshToken("valid-token")).thenReturn(userId);
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.empty());

        assertThrows(InvalidRefreshTokenException.class,
                () -> useCase.execute(new RefreshTokenCommand("valid-token")));

        verify(repository, never()).findAuthUserById(any());
        assertNoBusinessLog(output);
        assertNoSensitiveData(output);
    }

    @Test
    void throwsRefreshTokenExceptionWhenUserDoesNotExist(CapturedOutput output) {
        UUID userId = UUID.randomUUID();
        RefreshTokenEntity existingToken = new RefreshTokenEntity("valid-token", Instant.now().plusSeconds(3600));

        when(tokenProviderPort.validateRefreshToken("valid-token")).thenReturn(userId);
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(existingToken));
        when(repository.findAuthUserById(userId)).thenReturn(Optional.empty());

        assertThrows(RefreshTokenException.class,
                () -> useCase.execute(new RefreshTokenCommand("valid-token")));

        verify(tokenProviderPort, never()).generateAccessToken(any(), any());
        assertNoBusinessLog(output);
        assertNoSensitiveData(output);
    }

    @Test
    void throwsRefreshTokenExceptionWhenUserIsInactive(CapturedOutput output) {
        UUID userId = UUID.randomUUID();
        AuthUser user = Mockito.mock(AuthUser.class);
        RefreshTokenEntity existingToken = new RefreshTokenEntity("valid-token", Instant.now().plusSeconds(3600));

        when(user.isActive()).thenReturn(false);
        when(tokenProviderPort.validateRefreshToken("valid-token")).thenReturn(userId);
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(existingToken));
        when(repository.findAuthUserById(any())).thenReturn(Optional.of(user));

        assertThrows(RefreshTokenException.class,
                () -> useCase.execute(new RefreshTokenCommand("valid-token")));

        verify(tokenProviderPort, never()).generateAccessToken(any(), any());

        assertThat(output).contains("inativo ou bloqueado");
        assertNoSensitiveData(output);
    }

    @Test
    void fetchesCurrentMembershipsBeforeGeneratingAccessToken(CapturedOutput output) {
        UUID userId = UUID.randomUUID();
        AuthUser user = activeUser(userId);
        RefreshTokenEntity existingToken = new RefreshTokenEntity("valid-token", Instant.now().plusSeconds(3600));

        when(tokenProviderPort.validateRefreshToken("valid-token")).thenReturn(userId);
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(existingToken));
        when(repository.findAuthUserById(userId)).thenReturn(Optional.of(user));
        when(membershipRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(tokenProviderPort.generateAccessToken(any(), any())).thenReturn("access");
        when(tokenProviderPort.generateRefreshToken(any())).thenReturn("refresh");
        when(tokenProviderPort.getAccessTokenExpirationMs()).thenReturn(900_000L);
        when(tokenProviderPort.getRefreshTokenExpirationMs()).thenReturn(604_800_000L);

        useCase.execute(new RefreshTokenCommand("valid-token"));

        verify(membershipRepository).findAllByUserId(userId);
        assertNoSensitiveData(output);
    }

    @Test
    void deletesOldTokenAndSavesNewOneOnSuccess(CapturedOutput output) {
        UUID userId = UUID.randomUUID();
        AuthUser user = activeUser(userId);
        RefreshTokenEntity existingToken = new RefreshTokenEntity("valid-token", Instant.now().plusSeconds(3600));

        when(tokenProviderPort.validateRefreshToken("valid-token")).thenReturn(userId);
        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(existingToken));
        when(repository.findAuthUserById(userId)).thenReturn(Optional.of(user));
        when(membershipRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(tokenProviderPort.generateAccessToken(any(), any())).thenReturn("access");
        when(tokenProviderPort.generateRefreshToken(any())).thenReturn("new-refresh");
        when(tokenProviderPort.getAccessTokenExpirationMs()).thenReturn(900_000L);
        when(tokenProviderPort.getRefreshTokenExpirationMs()).thenReturn(604_800_000L);

        useCase.execute(new RefreshTokenCommand("valid-token"));

        verify(refreshTokenRepository).delete(existingToken);
        verify(refreshTokenRepository).save(any(RefreshTokenEntity.class));
        assertNoSensitiveData(output);
    }

    private AuthUser activeUser(UUID id) {
        return new AuthUser() {
            public UUID getId() { return id; }
            public String getPasswordHash() { return "hash"; }
            public TypeUser getType() { return TypeUser.STUDENT; }
            public boolean isActive() { return true; }
        };
    }

    private void assertNoBusinessLog(CapturedOutput output) {
        assertThat(output).doesNotContain("renovado com sucesso");
        assertThat(output).doesNotContain("inativo ou bloqueado");
    }

    private void assertNoSensitiveData(CapturedOutput output) {
        String out = output.toString().toLowerCase();
        assertThat(out).doesNotContain("password");
        assertThat(out).doesNotContain("jwt");
        assertThat(out).doesNotContain("authorization");
        assertThat(out).doesNotContain("new-access-token");
        assertThat(out).doesNotContain("new-refresh-token");
        assertThat(out).doesNotContain("valid-token");
        assertThat(out).doesNotContain("expired-token");
        assertThat(out).doesNotContain("access-token");
        assertThat(out).doesNotContain("refresh-token");
    }
}