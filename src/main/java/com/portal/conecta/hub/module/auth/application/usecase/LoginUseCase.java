package com.portal.conecta.hub.module.auth.application.usecase;

import com.portal.conecta.hub.module.auth.application.command.LoginCommand;
import com.portal.conecta.hub.module.auth.domain.exception.AuthException;
import com.portal.conecta.hub.module.auth.domain.model.AuthUser;
import com.portal.conecta.hub.module.auth.domain.port.TokenProviderPort;
import com.portal.conecta.hub.module.auth.infrastructure.security.JwtProperties;
import com.portal.conecta.hub.module.auth.presentation.dto.LoginResponse;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final TokenProviderPort tokenProviderPort;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;

    public LoginResponse execute(LoginCommand command) {

        AuthUser user =  repository.findByEmail(command.email())
                .orElseThrow(() -> new AuthException("Email ou senha inválidos", HttpStatus.UNAUTHORIZED));

        if(!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new RuntimeException("Email ou senha inválidos");
        }

        String accessToken = tokenProviderPort.generateAccessToken(user);
        String refreshToken = tokenProviderPort.generateRefreshToken(user);

        return new LoginResponse(accessToken, refreshToken, 900L);
    }
}