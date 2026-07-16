package com.portal.conecta.hub.module.user.presentation.dto.response;

import com.portal.conecta.hub.module.user.domain.model.AccountStatus;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import java.time.Instant;
import java.util.UUID;

/**
 * Item de usuario retornado na listagem paginada.
 *
 * <p>Expoe dados basicos de identificacao e o estado persistido da conta para
 * clientes que precisam diferenciar usuarios ativos de outros estados.</p>
 */
public record ListUserResponse(
        UUID id,
        String name,
        String email,
        TypeUser typeUser,
        boolean active,
        AccountStatus accountStatus,
        Instant createdAt
) {

    public static ListUserResponse from(UserEntity user) {
        return new ListUserResponse(
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
