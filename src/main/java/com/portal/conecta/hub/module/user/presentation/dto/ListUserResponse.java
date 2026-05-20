package com.portal.conecta.hub.module.user.presentation.dto;

import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import java.time.Instant;
import java.util.UUID;

public record ListUserResponse(
        UUID id,
        String name,
        String email,
        TypeUser typeUser,
        boolean active,
        Instant createdAt
) {

    public static ListUserResponse from(UserEntity user) {
        return new ListUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getTypeUser(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
