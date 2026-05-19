package com.portal.conecta.hub.module.user.presentation.dto;

import com.portal.conecta.hub.module.user.domain.model.TypeUser;

public record CreateUserRequest(
        String name,
        String email,
        String password,
        TypeUser typeUser
) {
}
