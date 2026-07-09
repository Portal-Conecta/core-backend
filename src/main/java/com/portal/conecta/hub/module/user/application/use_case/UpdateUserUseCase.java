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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Atualiza dados de um usuário existente.
 *
 * <p>Valida permissão do requisitante sobre o usuário alvo.
 * O e-mail, quando informado, é validado pela {@link UserEmailPolicy}
 * e verificado quanto à unicidade excluindo o próprio usuário.
 * Apenas campos não nulos e diferentes do valor atual são alterados.
 *
 * @throws UserNotFoundException         se o usuário alvo ou o requisitante não forem encontrados.
 * @throws com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException se o requisitante não puder editar o usuário alvo.
 * @throws EmailAlreadyInUseException    se o novo e-mail já estiver em uso por outro usuário.
 * @throws com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException      se o e-mail informado for inválido ou não pertencer ao domínio esperado.
 */
@Component
@Slf4j
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

    /**
     * Executa a atualização do usuário alvo.
     *
     * @param command contém o ID do alvo e os campos a atualizar.
     * @return entidade atualizada e persistida.
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

        String email = command.email();

        if (email != null && !email.isBlank()) {
            email = userEmailPolicy.validateForUpdate(email, target.getTypeUser());

            if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, target.getId())) {
                throw new EmailAlreadyInUseException(email);
            }
        }

        UserEntity updateBy = userRepository.findById(context.userId())
                .orElseThrow(() -> new UserNotFoundException("Usuário autenticado não encontrado. "));



        List<String> changedFields = target.update(command.name(), email, command.avatarUrl(), updateBy);

        UserEntity saved = userRepository.save(target);

        log.info("Usuário atualizado com sucesso. targetUserId={}, changedFields={}",
                saved.getId(), changedFields);

        return saved;
    }
}
