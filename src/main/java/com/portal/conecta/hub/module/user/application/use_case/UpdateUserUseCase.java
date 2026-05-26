package com.portal.conecta.hub.module.user.application.use_case;

import com.portal.conecta.hub.module.user.application.command.UpdateUserCommand;
import com.portal.conecta.hub.module.user.domain.exception.EmailAlreadyInUseException;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.module.user.domain.validator.UserPermissionValidator;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class UpdateUserUseCase {

    private final UserRepository userRepository;
    private final UserPermissionValidator permissionValidator;
    private final RequestContextProvider requestProvider;

    public UpdateUserUseCase(UserRepository userRepository, UserPermissionValidator permissionValidator, RequestContextProvider requestProvider) {
        this.userRepository = userRepository;
        this.permissionValidator = permissionValidator;
        this.requestProvider = requestProvider;
    }

    @Transactional
    public UserEntity execute(UpdateUserCommand command) {
        RequestContext context = requestProvider.getRequestContext();

        UserEntity target = userRepository.findById(command.targetUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + command.targetUserId()));

        permissionValidator.validateCanEdit(
                context.userId(),
                context.userType(),
                target.getId(),
                target.getTypeUser()
        );

        if (command.email() != null && !command.email().isBlank()) {
            if (userRepository.existsByEmailAndIdNot(command.email().trim(), command.targetUserId())) {
                throw new EmailAlreadyInUseException("Email already in use: " + command.email());
            }
        }

        UserEntity updateBy = userRepository.findById(context.userId())
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found: " + context.userId()));

        target.update(command.name(), command.email(), command.avatarUrl(), updateBy);

        return userRepository.save(target);
    }
}
