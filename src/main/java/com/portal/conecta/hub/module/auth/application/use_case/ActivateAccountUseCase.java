package com.portal.conecta.hub.module.auth.application.use_case;

import com.portal.conecta.hub.module.auth.application.command.ActivateAccountCommand;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.AccountActivationToken;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.AccountActivationTokenPort;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Activates a user account from a valid account activation token.
 *
 * <p>The use case consumes a single-use token, validates its expiration, encodes
 * the new password and enables the user account in the same transaction.</p>
 */
@Component
public class ActivateAccountUseCase {

    private final AccountActivationTokenPort activationTokenPort;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ActivateAccountUseCase(
            AccountActivationTokenPort activationTokenPort,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.activationTokenPort = activationTokenPort;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Activates the account associated with the given command.
     *
     * @param command activation token and password chosen by the user
     * @throws InvalidUserDataException when the token is invalid, expired or already used
     * @throws UserNotFoundException when the associated user no longer exists
     */
    @Transactional
    public void execute(ActivateAccountCommand command) {
        AccountActivationToken activationToken = activationTokenPort.findByRawToken(command.token())
                .orElseThrow(() -> new InvalidUserDataException("Token de ativacao invalido."));

        if (activationToken.isUsed()) {
            throw new InvalidUserDataException("Token de ativacao ja utilizado.");
        }

        if (activationToken.isExpired(Instant.now())) {
            throw new InvalidUserDataException("Token de ativacao expirado.");
        }

        UserEntity user = userRepository.findById(activationToken.userId())
                .orElseThrow(UserNotFoundException::new);

        if (user.getDeletedAt() != null) {
            throw new UserNotFoundException();
        }

        user.activate(command.newPassword(), user, passwordEncoder);
        userRepository.save(user);
        activationTokenPort.markAsUsed(command.token());
    }
}
