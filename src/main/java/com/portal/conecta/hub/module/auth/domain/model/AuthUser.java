package com.portal.conecta.hub.module.auth.domain.model;

import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.shared.context.ContextClass;

import java.util.List;
import java.util.UUID;

public record AuthUser(
        UUID userId,
        TypeUser userType,
        List<ContextClass> classes
) {
}