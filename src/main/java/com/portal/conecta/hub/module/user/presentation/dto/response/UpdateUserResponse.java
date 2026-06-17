package com.portal.conecta.hub.module.user.presentation.dto.response;

import com.portal.conecta.hub.module.user.domain.model.UserEntity;

import java.time.Instant;
import java.util.UUID;

public record UpdateUserResponse(
        UUID id,
        String name,
        String email,
        String avatarUrl,
        String typeUser,
        Instant updatedAt
) {

    public static UpdateUserResponse from(UserEntity user){
        return new UpdateUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getTypeUser().name(),
                user.getUpdatedAt()
        );
    }
}
