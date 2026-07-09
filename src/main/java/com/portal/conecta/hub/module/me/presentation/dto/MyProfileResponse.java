package com.portal.conecta.hub.module.me.presentation.dto;

import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;

import java.util.UUID;

public record MyProfileResponse(
        UUID id,
        String name,
        String email,
        TypeUser typeUser,
        String avatarUrl
) {
    public static MyProfileResponse from(UserEntity user) {
        return new MyProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getTypeUser(),
                user.getAvatarUrl()
        );
    }
}