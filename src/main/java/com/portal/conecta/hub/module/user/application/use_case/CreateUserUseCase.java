package com.portal.conecta.hub.module.user.application.use_case;

import com.portal.conecta.hub.module.user.application.command.CreateUserCommand;
import com.portal.conecta.hub.module.user.domain.exception.EmailAlreadyInUseException;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.AccountActivationNotificationPort;
import com.portal.conecta.hub.module.user.domain.port.AccountActivationTokenPort;
import com.portal.conecta.hub.module.user.domain.port.AccountActivationTokenTtlPort;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.module.user.domain.policy.UserEmailPolicy;
import com.portal.conecta.hub.module.user.domain.validator.UserPermissionValidator;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Creates a user account pending activation.
 *
 * <p>The use case validates permissions and e-mail uniqueness, persists the
 * inactive user, creates a single-use activation token and requests delivery of
 * the activation notification.</p>
 */
@Component
@Slf4j
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final UserEmailPolicy userEmailPolicy;
    private final UserPermissionValidator permissionValidator;
    private final RequestContextProvider contextProvider;
    private final PasswordEncoder passwordEncoder;
    private final AccountActivationTokenPort activationTokenPort;
    private final AccountActivationNotificationPort activationNotificationPort;
    private final AccountActivationTokenTtlPort activationTokenTtlPort;

    public CreateUserUseCase(
            UserRepository userRepository,
            UserEmailPolicy userEmailPolicy,
            UserPermissionValidator permissionValidator,
            RequestContextProvider contextProvider,
            PasswordEncoder passwordEncoder,
            AccountActivationTokenPort activationTokenPort,
            AccountActivationNotificationPort activationNotificationPort,
            AccountActivationTokenTtlPort activationTokenTtlPort
    ) {
        this.userRepository = userRepository;
        this.userEmailPolicy = userEmailPolicy;
        this.permissionValidator = permissionValidator;
        this.contextProvider = contextProvider;
        this.passwordEncoder = passwordEncoder;
        this.activationTokenPort = activationTokenPort;
        this.activationNotificationPort = activationNotificationPort;
        this.activationTokenTtlPort = activationTokenTtlPort;
    }

    /**
     * Creates an inactive user and starts the account activation flow.
     *
     * @param command user creation data without password
     * @return persisted user pending activation
     */
    @Transactional
    public UserEntity execute(CreateUserCommand command) {
        CreateUserCommand validCommand = requireCommand(command);
        RequestContext context = contextProvider.getRequestContext();

        permissionValidator.validateCanCreate(context.userType(), validCommand.typeUser());
        String email = userEmailPolicy.validateForCreation(validCommand.email(), validCommand.typeUser());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyInUseException(email);
        }

        UserEntity authenticatedUser = findAuthenticatedUser(context);
        String unusablePasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());

        UserEntity user = UserEntity.createPendingActivation(
                validCommand.name(),
                email,
                unusablePasswordHash,
                validCommand.typeUser(),
                authenticatedUser
        );

        UserEntity saved = userRepository.save(user);
        Instant expiresAt = Instant.now().plus(activationTokenTtlPort.activationTokenTtl());
        String rawActivationToken = activationTokenPort.createToken(saved, expiresAt);
        activationNotificationPort.requestActivation(saved, rawActivationToken, expiresAt);

        log.info("Usuario criado pendente de ativacao. targetUserId={}, targetUserType={}",
                saved.getId(), saved.getTypeUser());

        return saved;
    }

    private CreateUserCommand requireCommand(CreateUserCommand command) {
        if (command == null) {
            throw new InvalidUserDataException("A requisicao de criacao de usuario e obrigatoria.");
        }

        return command;
    }

    private UserEntity findAuthenticatedUser(RequestContext context) {
        return userRepository.findById(context.userId())
                .orElseThrow(UserNotFoundException::new);
    }
}
