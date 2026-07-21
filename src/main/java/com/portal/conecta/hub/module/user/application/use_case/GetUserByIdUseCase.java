package com.portal.conecta.hub.module.user.application.use_case;

import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.AccountStatus;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * Busca um usuário por ID, exceto quando estiver pendente de exclusão.
 *
 * @throws UserNotFoundException se o usuário não existir ou estiver pendente de exclusão.
 */
@Component
public class GetUserByIdUseCase {

    private final UserRepository userRepository;

    public GetUserByIdUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * @param userId ID do usuário; não pode ser nulo.
     * @return usuário encontrado que não esteja pendente de exclusão.
     * @throws UserNotFoundException se não encontrado ou pendente de exclusão.
     */
    public UserEntity execute(UUID userId){
        Objects.requireNonNull(userId, "O identificador do usuário é obrigatório.");
        return userRepository.findByIdAndAccountStatusNot(userId, AccountStatus.PENDING_DELETION)
                .orElseThrow(UserNotFoundException::new);
    }
}
