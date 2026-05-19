package com.portal.conecta.hub.module.user.application.command;

import com.portal.conecta.hub.module.user.domain.model.TypeUser;

public record CreateUserCommand(
        String name,
        String email,
        String password,
        TypeUser typeUser
) {
}
