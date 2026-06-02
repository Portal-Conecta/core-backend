package com.portal.conecta.hub.module.auth.application.use_case;

import com.portal.conecta.hub.module.auth.application.command.LoginCommand;
import com.portal.conecta.hub.module.auth.domain.exception.AuthException;
import com.portal.conecta.hub.module.auth.domain.model.AuthUser;
import com.portal.conecta.hub.module.auth.domain.port.TokenProviderPort;
import com.portal.conecta.hub.module.auth.presentation.dto.LoginResponse;
import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.port.ClassMembershipRepository;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final TokenProviderPort tokenProviderPort;
    private final PasswordEncoder passwordEncoder;

    private final UserRepository repository;
    private final ClassMembershipRepository membershipRepository;

    public LoginResponse execute(LoginCommand command) {

        AuthUser user = repository.findByEmail(command.email())
                .orElseThrow(() -> new AuthException("Email ou senha inválidos"));

        List<ClassMembershipEntity> membershipEntities = membershipRepository.findAllByUserId(user.getId());

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new AuthException("Email ou senha inválidos");
        }

        String accessToken = tokenProviderPort.generateAccessToken(user, membershipEntities);
        String refreshToken = tokenProviderPort.generateRefreshToken(user);

        return new LoginResponse(accessToken, refreshToken, 900L);
    }
}