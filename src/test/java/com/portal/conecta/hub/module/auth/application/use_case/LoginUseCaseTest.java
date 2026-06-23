package com.portal.conecta.hub.module.auth.application.use_case;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.portal.conecta.hub.module.auth.application.command.LoginCommand;
import com.portal.conecta.hub.module.auth.application.result.LoginResult;
import com.portal.conecta.hub.module.auth.domain.exception.AuthException;
import com.portal.conecta.hub.module.auth.domain.exception.RefreshTokenException;
import com.portal.conecta.hub.module.auth.domain.model.AuthUser;
import com.portal.conecta.hub.module.auth.domain.port.RefreshTokenRepository;
import com.portal.conecta.hub.module.auth.domain.port.TokenProviderPort;
import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
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
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private LoginUseCase useCase;

    private static final String EMAIL = "usuario@senai.br";
    private static final String RAW_PASSWORD = "senha123";
    private static final String PASSWORD_HASH = "hash-senha";

    @BeforeEach
    void setUp() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.getLogger("com.portal.conecta.hub.module.auth.application.use_case").setLevel(Level.INFO);
    }

    @Test
    @DisplayName("deve retornar tokens e logar INFO quando usuário ativo informa credenciais válidas")
    void shouldReturnTokensWhenActiveUserWithValidCredentials(CapturedOutput output) {
        UUID userId = UUID.randomUUID();
        AuthUser user = mock(AuthUser.class);
        when(user.getId()).thenReturn(userId);
        when(user.getPasswordHash()).thenReturn(PASSWORD_HASH);
        when(user.getType()).thenReturn(TypeUser.STUDENT);
        when(user.isActive()).thenReturn(true);

        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(true);
        when(membershipRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(tokenProviderPort.generateAccessToken(eq(user), any())).thenReturn("access-token");
        when(tokenProviderPort.generateRefreshToken(user)).thenReturn("refresh-token");
        when(tokenProviderPort.getRefreshTokenExpirationMs()).thenReturn(604800000L);
        when(tokenProviderPort.getAccessTokenExpirationMs()).thenReturn(900000L);

        LoginResult result = useCase.execute(new LoginCommand(EMAIL, RAW_PASSWORD));

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.expiresIn()).isEqualTo(900L);

        verify(refreshTokenRepository).save(any());

        assertThat(output).contains("realizado com sucesso");
        assertNoSensitiveData(output);
    }

    @Test
    @DisplayName("deve lançar AuthException sem log de negócio quando e-mail não existe")
    void shouldThrowWhenEmailNotFound(CapturedOutput output) {
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new LoginCommand(EMAIL, RAW_PASSWORD)))
                .isInstanceOf(AuthException.class);

        verifyNoInteractions(refreshTokenRepository, tokenProviderPort, membershipRepository);
        assertNoBusinessLog(output);
        assertNoSensitiveData(output);
    }

    @Test
    @DisplayName("deve lançar AuthException sem log de negócio quando senha está incorreta")
    void shouldThrowWhenPasswordIsIncorrect(CapturedOutput output) {
        AuthUser user = mock(AuthUser.class);
        when(user.getPasswordHash()).thenReturn(PASSWORD_HASH);

        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(new LoginCommand(EMAIL, RAW_PASSWORD)))
                .isInstanceOf(AuthException.class);

        verifyNoInteractions(refreshTokenRepository, tokenProviderPort, membershipRepository);
        assertNoBusinessLog(output);
        assertNoSensitiveData(output);
    }

    @Test
    @DisplayName("deve lançar RefreshTokenException e logar WARN quando usuário está inativo")
    void shouldThrowWhenUserIsInactive(CapturedOutput output) {
        AuthUser user = mock(AuthUser.class);
        when(user.getPasswordHash()).thenReturn(PASSWORD_HASH);
        when(user.isActive()).thenReturn(false);

        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(new LoginCommand(EMAIL, RAW_PASSWORD)))
                .isInstanceOf(RefreshTokenException.class);

        verifyNoInteractions(refreshTokenRepository, tokenProviderPort, membershipRepository);

        assertThat(output).contains("inativo ou bloqueado");
        assertNoSensitiveData(output);
    }

    private void assertNoBusinessLog(CapturedOutput output) {
        assertThat(output).doesNotContain("realizado com sucesso");
        assertThat(output).doesNotContain("inativo ou bloqueado");
    }

    private void assertNoSensitiveData(CapturedOutput output) {
        String out = output.toString().toLowerCase();
        assertThat(out).doesNotContain("password");
        assertThat(out).doesNotContain("jwt");
        assertThat(out).doesNotContain("authorization");
        assertThat(out).doesNotContain(EMAIL.toLowerCase());
        assertThat(out).doesNotContain("access-token");
        assertThat(out).doesNotContain("refresh-token");
    }
}