package com.portal.conecta.hub.module.user.presentation.dto.response;

import com.portal.conecta.hub.module.user.domain.model.AccountStatus;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import java.time.Instant;
import java.util.UUID;

/**
 * Resposta publica de usuario para consultas unitarias e em lote.
 *
 * <p>Mantem o booleano {@code active} por compatibilidade e expoe
 * {@code accountStatus} como fonte explicita do estado da conta.</p>
 */
public record UserResponse(
        UUID id,
        String name,
        String email,
        TypeUser typeUser,
        boolean active,
        AccountStatus accountStatus,
        Instant createdAt
) {
    public static UserResponse from(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getTypeUser(),
                user.isActive(),
                user.getAccountStatus(),
                user.getCreatedAt()
        );
    }
}
