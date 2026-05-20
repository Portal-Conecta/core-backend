package com.portal.conecta.hub.module.user.application.query;

import com.portal.conecta.hub.module.user.domain.model.TypeUser;

public record GetAllUserQuery(
        int page,
        int size,
        TypeUser typeUser
) {
}
