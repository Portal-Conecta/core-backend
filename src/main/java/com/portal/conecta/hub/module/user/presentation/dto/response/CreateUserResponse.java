package com.portal.conecta.hub.module.user.presentation.dto.response;

import com.portal.conecta.hub.module.user.domain.model.AccountStatus;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import java.time.Instant;
import java.util.UUID;

/**
 * Resposta retornada apos a criacao de uma conta de usuario.
 *
 * <p>Inclui o status persistido da conta para indicar que novos usuarios do
 * fluxo administrativo nascem pendentes de ativacao.</p>
 */
public record CreateUserResponse(
        UUID id,
        String name,
        String email,
        TypeUser typeUser,
        boolean active,
        AccountStatus accountStatus,
        Instant createdAt,
        Instant deletedAt
) {

    public static CreateUserResponse from(UserEntity user) {
        return new CreateUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getTypeUser(),
                user.isActive(),
                user.getAccountStatus(),
                user.getCreatedAt(),
                user.getDeletedAt()
        );
    }
}
