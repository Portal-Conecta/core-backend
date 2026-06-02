package com.portal.conecta.hub.module.auth.application.use_case;

import com.portal.conecta.hub.module.auth.application.command.RefreshTokenCommand;
import com.portal.conecta.hub.module.auth.application.result.RefreshTokenResult;
import com.portal.conecta.hub.module.auth.domain.exception.RefreshTokenException;
import com.portal.conecta.hub.module.auth.domain.model.AuthUser;
import com.portal.conecta.hub.module.auth.domain.port.TokenProviderPort;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {

    private final TokenProviderPort tokenProviderPort;
    private final UserRepository repository;
    private final ClassMembershipRepository membershipRepository;

    public RefreshTokenResult execute(RefreshTokenCommand command) {

        UUID userId = tokenProviderPort.validateRefreshToken(command.refreshToken());

        AuthUser user = repository.findAuthUserById(userId)
                .orElseThrow(() -> new RefreshTokenException("User not found"));

        if (!user.isActive()) {
            throw new RefreshTokenException("User is inactive or blocked");
        }

        List<ClassMembershipEntity> membershipEntities = membershipRepository.findAllByUserId(user.getId());

        String accessToken = tokenProviderPort.generateAccessToken(user, membershipEntities);
        String refreshToken = tokenProviderPort.generateRefreshToken(user);

        Long accessTokenExpiration = tokenProviderPort.getAccessTokenExpirationMs() / 1000;

        return new RefreshTokenResult (accessToken, refreshToken, accessTokenExpiration);
    }

}