package com.portal.conecta.hub.module.auth.application.use_case;

import com.portal.conecta.hub.module.auth.application.command.RefreshTokenCommand;
import com.portal.conecta.hub.module.auth.application.result.RefreshTokenResult;
import com.portal.conecta.hub.module.auth.domain.exception.AuthException;
import com.portal.conecta.hub.module.auth.domain.exception.InvalidRefreshTokenException;
import com.portal.conecta.hub.module.auth.domain.exception.RefreshTokenException;
import com.portal.conecta.hub.module.auth.domain.model.AuthUser;
import com.portal.conecta.hub.module.auth.domain.model.RefreshTokenEntity;
import com.portal.conecta.hub.module.auth.domain.port.RefreshTokenRepository;
import com.portal.conecta.hub.module.auth.domain.port.TokenProviderPort;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Caso de uso responsável por renovar a sessão do usuário a partir de um refresh token válido.
 *
 * <p>Implementa rotação de token: o refresh token recebido é invalidado imediatamente
 * após validação e um novo par (access + refresh) é gerado e persistido.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenUseCase {

    private final TokenProviderPort tokenProviderPort;
    private final UserRepository repository;
    private final ClassMembershipRepository membershipRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Executa a renovação de sessão via refresh token.
     *
     * @param command comando contendo o refresh token recebido do cliente
     * @return {@link RefreshTokenResult} com novo access token, novo refresh token e
     *         expiração do access token em segundos
     * @throws AuthException                 se o refresh token tiver assinatura inválida,
     *                                       estiver expirado ou não for do tipo {@code refresh}
     * @throws InvalidRefreshTokenException  se o token não for encontrado na base de dados
     *                                       (já rotacionado, revogado ou inexistente)
     * @throws RefreshTokenException         se o usuário não for encontrado ou estiver inativo/bloqueado
     */
    public RefreshTokenResult execute(RefreshTokenCommand command) {

        UUID userId = tokenProviderPort.validateRefreshToken(command.refreshToken());

        RefreshTokenEntity existingToken = refreshTokenRepository.findByToken(command.refreshToken())
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token inválido ou expirado"));

        refreshTokenRepository.delete(existingToken);

        AuthUser user = repository.findAuthUserById(userId)
                .orElseThrow(() -> new RefreshTokenException("Usuário não encontrado"));

        if (!user.isActive()) {
            throw new RefreshTokenException("Usuário está inativo ou bloqueado");
        }

        List<ClassMembershipEntity> membershipEntities = membershipRepository.findAllByUserId(user.getId());

        String accessToken = tokenProviderPort.generateAccessToken(user, membershipEntities);
        String refreshToken = tokenProviderPort.generateRefreshToken(user);

        Instant expiresAt = Instant.now().plusMillis(tokenProviderPort.getRefreshTokenExpirationMs());
        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity(refreshToken, expiresAt);
        refreshTokenRepository.save(refreshTokenEntity);

        log.info("Refresh token renovado com sucesso.");

        Long accessTokenExpiration = tokenProviderPort.getAccessTokenExpirationMs() / 1000;

        return new RefreshTokenResult(accessToken, refreshToken, accessTokenExpiration);
    }
}