package com.portal.conecta.hub.module.auth.application.use_case;

import com.portal.conecta.hub.module.auth.application.command.LogoutCommand;
import com.portal.conecta.hub.module.auth.domain.exception.AuthException;
import com.portal.conecta.hub.module.auth.domain.exception.InvalidRefreshTokenException;
import com.portal.conecta.hub.module.auth.domain.model.RefreshTokenEntity;
import com.portal.conecta.hub.module.auth.domain.port.RefreshTokenRepository;
import com.portal.conecta.hub.module.auth.domain.port.TokenProviderPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

    @Mock
    private TokenProviderPort tokenProviderPort;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private LogoutUseCase logoutUseCase;

    private static final String VALID_TOKEN = "valid.refresh.token";

    @Test
    void deveLancarAuthExceptionQuandoTokenForEstruturalmenteInvalido() {
        doThrow(new AuthException("Refresh token inválido ou expirado"))
                .when(tokenProviderPort).validateRefreshToken(VALID_TOKEN);

        assertThatThrownBy(() -> logoutUseCase.execute(new LogoutCommand(VALID_TOKEN)))
                .isInstanceOf(AuthException.class);

        verify(refreshTokenRepository, never()).findByToken(any());
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void deveLancarInvalidRefreshTokenExceptionQuandoTokenNaoExistirNoBanco() {
        when(refreshTokenRepository.findByToken(VALID_TOKEN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> logoutUseCase.execute(new LogoutCommand(VALID_TOKEN)))
                .isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void deveRemoverTokenERetornarSemEmitirNovosTokensQuandoTokenForValido() {
        RefreshTokenEntity entity = mock(RefreshTokenEntity.class);
        when(refreshTokenRepository.findByToken(VALID_TOKEN)).thenReturn(Optional.of(entity));

        logoutUseCase.execute(new LogoutCommand(VALID_TOKEN));

        verify(refreshTokenRepository).delete(entity);
        verifyNoMoreInteractions(refreshTokenRepository);
    }

    @Test
    void naoDeveConsultarUsuarioNemTurmasDuranteLogout() {
        RefreshTokenEntity entity = mock(RefreshTokenEntity.class);
        when(refreshTokenRepository.findByToken(VALID_TOKEN)).thenReturn(Optional.of(entity));

        logoutUseCase.execute(new LogoutCommand(VALID_TOKEN));

        verify(tokenProviderPort).validateRefreshToken(VALID_TOKEN);
        verifyNoMoreInteractions(tokenProviderPort);
    }
}