package com.portal.conecta.hub.module.classes.application.query;

import com.portal.conecta.hub.module.classes.domain.model.ClassRole;

import java.util.UUID;

public record GetClassMembersQuery(
        UUID classId,
        ClassRole role
) {
    public static GetClassMembersQuery from(UUID classId, ClassRole role) {
        return new GetClassMembersQuery(classId, role);
    }
}
