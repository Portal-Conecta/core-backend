package com.portal.conecta.hub.module.auth.application.use_case;

import com.portal.conecta.hub.module.auth.application.command.LogoutCommand;
import com.portal.conecta.hub.module.auth.domain.exception.InvalidRefreshTokenException;
import com.portal.conecta.hub.module.auth.domain.model.RefreshTokenEntity;
import com.portal.conecta.hub.module.auth.domain.port.RefreshTokenRepository;
import com.portal.conecta.hub.module.auth.domain.port.TokenProviderPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LogoutUseCase {

    private final TokenProviderPort tokenProviderPort;
    private final RefreshTokenRepository refreshTokenRepository;

    public LogoutUseCase(TokenProviderPort tokenProviderPort, RefreshTokenRepository refreshTokenRepository) {
        this.tokenProviderPort = tokenProviderPort;
        this.refreshTokenRepository = refreshTokenRepository;
    }


    public void execute (LogoutCommand command){
        tokenProviderPort.validateRefreshToken(command.refreshToken());

        RefreshTokenEntity existingToken = refreshTokenRepository.findByToken(command.refreshToken())
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh Token inválido ou expirado."));

        refreshTokenRepository.delete(existingToken);

        log.info("Sessão encerrada com sucesso.");
    }
}
