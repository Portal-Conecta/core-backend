package com.portal.conecta.hub.module.user.application.use_case;

import com.portal.conecta.hub.module.user.application.command.CreateUserCommand;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.module.user.domain.policy.UserEmailPolicy;
import com.portal.conecta.hub.module.user.domain.validator.UserPermissionValidator;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final UserEmailPolicy userEmailPolicy;
    private final UserPermissionValidator permissionValidator;
    private final RequestContextProvider contextProvider;
    private final PasswordEncoder passwordEncoder;

    public CreateUserUseCase(
            UserRepository userRepository,
            UserEmailPolicy userEmailPolicy,
            UserPermissionValidator permissionValidator,
            RequestContextProvider contextProvider,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userEmailPolicy = userEmailPolicy;
        this.permissionValidator = permissionValidator;
        this.contextProvider = contextProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserEntity execute(CreateUserCommand command) {
        CreateUserCommand validCommand = requireCommand(command);
        RequestContext context = contextProvider.getRequestContext();

        permissionValidator.validateCanCreate(context.userType(), validCommand.typeUser());
        String email = userEmailPolicy.validateForCreation(validCommand.email());
        UserEntity authenticatedUser = findAuthenticatedUser(context);

        UserEntity user = UserEntity.create(
                validCommand.name(),
                email,
                validCommand.password(),
                validCommand.typeUser(),
                authenticatedUser,
                passwordEncoder
        );

        return userRepository.save(user);
    }

    private CreateUserCommand requireCommand(CreateUserCommand command) {
        if (command == null) {
            throw new InvalidUserDataException("Create user request is required.");
        }

        return command;
    }

    private UserEntity findAuthenticatedUser(RequestContext context) {
        return userRepository.findById(context.userId())
                .orElseThrow(UserNotFoundException::new);
    }
}
