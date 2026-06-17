package com.portal.conecta.hub.module.user.application.use_case;

import com.portal.conecta.hub.module.user.application.command.UpdateUserCommand;
import com.portal.conecta.hub.module.user.domain.exception.EmailAlreadyInUseException;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.policy.UserEmailPolicy;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.module.user.domain.validator.UserPermissionValidator;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class UpdateUserUseCase {

    private final UserRepository userRepository;
    private final UserPermissionValidator permissionValidator;
    private final RequestContextProvider requestProvider;
    private final UserEmailPolicy userEmailPolicy;

    public UpdateUserUseCase(UserRepository userRepository, UserPermissionValidator permissionValidator, RequestContextProvider requestProvider, UserEmailPolicy userEmailPolicy) {
        this.userRepository = userRepository;
        this.permissionValidator = permissionValidator;
        this.requestProvider = requestProvider;
        this.userEmailPolicy = userEmailPolicy;
    }

    @Transactional
    public UserEntity execute(UpdateUserCommand command) {
        RequestContext context = requestProvider.getRequestContext();

        UserEntity target = userRepository.findById(command.targetUserId())
                .orElseThrow(UserNotFoundException::new);

        permissionValidator.validateCanEdit(
                context.userId(),
                context.userType(),
                target.getId(),
                target.getTypeUser()
        );

        String email = command.email();

        if (email != null && !email.isBlank()) {
            email = userEmailPolicy.validateForUpdate(email, target.getTypeUser());

            if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, target.getId())) {
                throw new EmailAlreadyInUseException(email);
            }
        }

        UserEntity updateBy = userRepository.findById(context.userId())
                .orElseThrow(() -> new UserNotFoundException("Usuário autenticado não encontrado. "));

        target.update(command.name(), command.email(), command.avatarUrl(), updateBy);

        return userRepository.save(target);
    }
}
