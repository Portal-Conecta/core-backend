package com.portal.conecta.hub.module.user.application.use_case;

import com.portal.conecta.hub.module.user.application.command.UpdateUserCommand;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.module.user.domain.validator.UserPermissionValidator;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Atualiza o nome de um usuário existente.
 *
 * <p>Valida a permissão do requisitante sobre o usuário alvo antes de aplicar a alteração.</p>
 */
@Component
@Slf4j
public class UpdateUserUseCase {

    private final UserRepository userRepository;
    private final UserPermissionValidator permissionValidator;
    private final RequestContextProvider requestProvider;

    public UpdateUserUseCase(
            UserRepository userRepository,
            UserPermissionValidator permissionValidator,
            RequestContextProvider requestProvider
    ) {
        this.userRepository = userRepository;
        this.permissionValidator = permissionValidator;
        this.requestProvider = requestProvider;
    }

    /**
     * Executa a atualização do usuário alvo.
     *
     * @param command contém o ID do alvo e o nome a atualizar.
     * @return entidade atualizada e persistida.
     * @throws UserNotFoundException se o usuário alvo ou o requisitante não forem encontrados.
     */
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

        UserEntity updatedBy = userRepository.findById(context.userId())
                .orElseThrow(() -> new UserNotFoundException("Usuário autenticado não encontrado."));

        var changedFields = target.update(command.name(), updatedBy);
        UserEntity saved = userRepository.save(target);

        log.info("Usuário atualizado com sucesso. targetUserId={}, changedFields={}",
                saved.getId(), changedFields);

        return saved;
    }
}
