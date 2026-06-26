package com.portal.conecta.hub.module.user.application.use_case;

import com.portal.conecta.hub.module.user.application.command.CreateUserCommand;
import com.portal.conecta.hub.module.user.domain.exception.EmailAlreadyInUseException;
import com.portal.conecta.hub.module.user.domain.exception.InvalidUserDataException;
import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import com.portal.conecta.hub.module.user.domain.policy.UserEmailPolicy;
import com.portal.conecta.hub.module.user.domain.validator.UserPermissionValidator;
import com.portal.conecta.hub.shared.context.RequestContext;
import com.portal.conecta.hub.shared.context.RequestContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cria um novo usuário após validar permissão, e-mail e unicidade.
 *
 * <p>Fluxo: valida permissão do requisitante → valida e normaliza e-mail
 * pela {@link UserEmailPolicy} → verifica unicidade → cria e persiste o usuário.
 *
 * @throws com.portal.conecta.hub.module.user.domain.exception.UserPermissionDeniedException se o requisitante não puder criar o tipo solicitado.
 * @throws InvalidUserDataException      se e-mail ou dados obrigatórios forem inválidos.
 * @throws EmailAlreadyInUseException    se o e-mail já estiver em uso.
 */
@Component
@Slf4j
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

    /**
     * Executa a criação do usuário.
     *
     * @param command dados necessários para criação; não pode ser nulo.
     * @return entidade persistida.
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

        UserEntity user = UserEntity.create(
                validCommand.name(),
                email,
                validCommand.password(),
                validCommand.typeUser(),
                authenticatedUser,
                passwordEncoder
        );

        UserEntity saved = userRepository.save(user);

        log.info("Usuário criado com sucesso. targetUserId={}, targetUserType={}",
                saved.getId(), saved.getTypeUser());

        return saved;
    }

    private CreateUserCommand requireCommand(CreateUserCommand command) {
        if (command == null) {
            throw new InvalidUserDataException("A requisição de criação de usuário é obrigatória.");
        }

        return command;
    }

    private UserEntity findAuthenticatedUser(RequestContext context) {
        return userRepository.findById(context.userId())
                .orElseThrow(UserNotFoundException::new);
    }
}
