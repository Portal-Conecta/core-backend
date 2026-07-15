package com.portal.conecta.hub.module.user.application.use_case;

import com.portal.conecta.hub.module.user.application.command.DeleteUserCommand;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.exception.UserAlreadyInactiveException;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.module.user.domain.validator.UserPermissionValidator;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class DeleteUserUseCase {

    private final UserRepository userRepository;
    private final UserPermissionValidator permissionValidator;
    private final RequestContextProvider contextProvider;

    public DeleteUserUseCase(
            UserRepository userRepository,
            UserPermissionValidator permissionValidator,
            RequestContextProvider contextProvider
    ) {
        this.userRepository = userRepository;
        this.permissionValidator = permissionValidator;
        this.contextProvider = contextProvider;
    }

    @Transactional
    public UserEntity execute(DeleteUserCommand command) {
        if (command == null || command.targetUserId() == null) {
            throw new InvalidUserDataException("O ID do usuario de destino e obrigatorio.");
        }

        RequestContext context = contextProvider.getRequestContext();
        UserEntity targetUser = userRepository.findById(command.targetUserId())
                .orElseThrow(UserNotFoundException::new);

        if (targetUser.isRemoved()) {
            throw new UserAlreadyInactiveException();
        }

        boolean isSelf = context.userId().equals(command.targetUserId());
        if (!isSelf) {
            permissionValidator.validateCanDeactivate(context.userType(), targetUser.getTypeUser());
        }

        UserEntity requester = userRepository.findById(context.userId())
                .orElseThrow(UserNotFoundException::new);

        targetUser.delete(requester);
        UserEntity saved = userRepository.save(targetUser);
        log.info("Usuario marcado para exclusao. targetUserId={}", command.targetUserId());
        return saved;
    }
}
