package com.portal.conecta.hub.shared.context;

import com.portal.conecta.hub.module.classes.domain.model.ClassRole;

import java.util.UUID;

public record ContextClass(
        UUID classId,
        ClassRole role
) {
}