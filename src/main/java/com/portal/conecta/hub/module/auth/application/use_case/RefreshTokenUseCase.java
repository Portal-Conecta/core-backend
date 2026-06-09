package com.portal.conecta.hub.module.auth.application.use_case;

import com.portal.conecta.hub.module.auth.application.command.RefreshTokenCommand;
import com.portal.conecta.hub.module.auth.application.result.RefreshTokenResult;
import com.portal.conecta.hub.module.auth.domain.exception.RefreshTokenException;
import com.portal.conecta.hub.module.auth.domain.model.AuthUser;
import com.portal.conecta.hub.module.auth.domain.model.RefreshTokenEntity;
import com.portal.conecta.hub.module.auth.domain.port.RefreshTokenRepository;
import com.portal.conecta.hub.module.auth.domain.port.TokenProviderPort;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {

    private final TokenProviderPort tokenProviderPort;
    private final UserRepository repository;
    private final ClassMembershipRepository membershipRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenResult execute(RefreshTokenCommand command) {

        UUID userId = tokenProviderPort.validateRefreshToken(command.refreshToken());

        RefreshTokenEntity existingToken = refreshTokenRepository.findByToken(command.refreshToken())
                .orElseThrow(() -> new RefreshTokenException("Refresh token not found"));

        refreshTokenRepository.delete(existingToken);

        AuthUser user = repository.findAuthUserById(userId)
                .orElseThrow(() -> new RefreshTokenException("User not found"));

        if (!user.isActive()) {
            throw new RefreshTokenException("User is inactive or blocked");
        }

        List<ClassMembershipEntity> membershipEntities = membershipRepository.findAllByUserId(user.getId());

        String accessToken = tokenProviderPort.generateAccessToken(user, membershipEntities);
        String refreshToken = tokenProviderPort.generateRefreshToken(user);

        Instant expiresAt = Instant.now().plusMillis(tokenProviderPort.getRefreshTokenExpirationMs());
        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity(refreshToken, expiresAt);
        refreshTokenRepository.save(refreshTokenEntity);

        Long accessTokenExpiration = tokenProviderPort.getAccessTokenExpirationMs() / 1000;

        return new RefreshTokenResult (accessToken, refreshToken, accessTokenExpiration);
    }

}