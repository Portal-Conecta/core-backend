package com.portal.conecta.hub.module.user.application.use_case;

import com.portal.conecta.hub.module.user.application.command.ReactivateUserCommand;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.AccountStatus;
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
public class ReactivateUserUseCase {

    private final UserRepository userRepository;
    private final UserPermissionValidator permissionValidator;
    private final RequestContextProvider contextProvider;

    public ReactivateUserUseCase(
            UserRepository userRepository,
            UserPermissionValidator permissionValidator,
            RequestContextProvider contextProvider
    ) {
        this.userRepository = userRepository;
        this.permissionValidator = permissionValidator;
        this.contextProvider = contextProvider;
    }

    @Transactional
    public UserEntity execute(ReactivateUserCommand command) {
        if (command == null || command.targetUserId() == null) {
            throw new InvalidUserDataException("O ID do usuario de destino e obrigatorio.");
        }

        RequestContext context = contextProvider.getRequestContext();
        UserEntity targetUser = userRepository.findById(command.targetUserId())
                .orElseThrow(UserNotFoundException::new);

        if (targetUser.getAccountStatus() == AccountStatus.PENDING_DELETION) {
            throw new UserNotFoundException();
        }
        if (targetUser.getAccountStatus() != AccountStatus.DISABLED) {
            throw new InvalidUserDataException("Somente usuarios desativados podem ser reativados.");
        }

        boolean isSelf = context.userId().equals(command.targetUserId());
        if (!isSelf) {
            permissionValidator.validateCanDeactivate(context.userType(), targetUser.getTypeUser());
        }

        UserEntity requester = userRepository.findById(context.userId())
                .orElseThrow(UserNotFoundException::new);

        targetUser.reactivate(requester);
        UserEntity saved = userRepository.save(targetUser);
        log.info("Usuario reativado operacionalmente. targetUserId={}", command.targetUserId());
        return saved;
    }
}
