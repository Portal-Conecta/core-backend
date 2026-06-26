package com.portal.conecta.hub.module.user.application.use_case;

import com.portal.conecta.hub.module.user.domain.exception.UserNotFoundException;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * Busca um usuário ativo pelo ID.
 *
 * @throws UserNotFoundException se o usuário não existir, estiver inativo ou excluído.
 */
@Component
public class GetUserByIdUseCase {

    private final UserRepository userRepository;

    public GetUserByIdUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * @param userId ID do usuário; não pode ser nulo.
     * @return usuário ativo encontrado.
     * @throws UserNotFoundException se não encontrado, inativo ou excluído.
     */
    public UserEntity execute(UUID userId){
        Objects.requireNonNull(userId, "O identificador do usuário é obrigatório.");
        return userRepository.findByIdAndDeletedAtIsNullAndActiveTrue(userId)
                .orElseThrow(UserNotFoundException::new);
    }
}
