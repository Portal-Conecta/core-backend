package com.portal.conecta.hub.module.auth.application.use_case;

import com.portal.conecta.hub.module.auth.application.command.LoginCommand;
import com.portal.conecta.hub.module.auth.application.result.LoginResult;
import com.portal.conecta.hub.module.auth.domain.exception.AuthException;
import com.portal.conecta.hub.module.auth.domain.model.AuthUser;
import com.portal.conecta.hub.module.auth.domain.model.RefreshTokenEntity;
import com.portal.conecta.hub.module.auth.domain.port.RefreshTokenRepository;
import com.portal.conecta.hub.module.auth.domain.port.TokenProviderPort;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final TokenProviderPort tokenProviderPort;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;
    private final ClassMembershipRepository membershipRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public LoginResult execute(LoginCommand command) {

        AuthUser user = repository.findByEmail(command.email())
                .orElseThrow(() -> new AuthException("E-mail ou senha inválidos"));

        List<ClassMembershipEntity> membershipEntities = membershipRepository.findAllByUserId(user.getId());

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new AuthException("E-mail ou senha inválidos");
        }

        String accessToken = tokenProviderPort.generateAccessToken(user, membershipEntities);
        String refreshToken = tokenProviderPort.generateRefreshToken(user);

        Instant expiresAt = Instant.now().plusMillis(tokenProviderPort.getRefreshTokenExpirationMs());
        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity(refreshToken, expiresAt);
        refreshTokenRepository.save(refreshTokenEntity);

        Long accessTokenExpiration = tokenProviderPort.getAccessTokenExpirationMs() / 1000;

        return new LoginResult(accessToken, refreshToken, accessTokenExpiration);
    }
}