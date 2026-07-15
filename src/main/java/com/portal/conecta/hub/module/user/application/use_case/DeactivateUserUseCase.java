package com.portal.conecta.hub.module.user.application.use_case;

import com.portal.conecta.hub.module.user.application.command.DeactivateUserCommand;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.exception.UserAlreadyInactiveException;
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

/**
 * Caso de uso responsavel por desativar operacionalmente uma conta de usuario.
 *
 * <p>A transicao permitida e de {@link AccountStatus#ACTIVE} para
 * {@link AccountStatus#DISABLED}. A operacao nao preenche {@code deletedAt},
 * pois nao inicia a janela de exclusao fisica.</p>
 */
@Slf4j
@Component
public class DeactivateUserUseCase {

    private final UserRepository userRepository;
    private final UserPermissionValidator permissionValidator;
    private final RequestContextProvider contextProvider;

    public DeactivateUserUseCase(
            UserRepository userRepository,
            UserPermissionValidator permissionValidator,
            RequestContextProvider contextProvider
    ) {
        this.userRepository = userRepository;
        this.permissionValidator = permissionValidator;
        this.contextProvider = contextProvider;
    }

    @Transactional
    public UserEntity execute(DeactivateUserCommand command) {
        if (command == null || command.targetUserId() == null) {
            throw new InvalidUserDataException("O ID do usuario de destino e obrigatorio.");
        }

        RequestContext context = contextProvider.getRequestContext();
        UserEntity targetUser = userRepository.findById(command.targetUserId())
                .orElseThrow(UserNotFoundException::new);

        if (targetUser.getAccountStatus() == AccountStatus.PENDING_DELETION) {
            throw new UserNotFoundException();
        }
        if (targetUser.getAccountStatus() == AccountStatus.DISABLED) {
            throw new UserAlreadyInactiveException();
        }
        if (targetUser.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new InvalidUserDataException("Somente usuarios ativos podem ser desativados.");
        }

        boolean isSelf = context.userId().equals(command.targetUserId());
        if (!isSelf) {
            permissionValidator.validateCanDeactivate(context.userType(), targetUser.getTypeUser());
        }

        UserEntity requester = userRepository.findById(context.userId())
                .orElseThrow(UserNotFoundException::new);

        targetUser.deactivate(requester);
        UserEntity saved = userRepository.save(targetUser);
        log.info("Usuario desativado operacionalmente. targetUserId={}", command.targetUserId());
        return saved;
    }
}
