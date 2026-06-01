package com.portal.conecta.hub.module.classes.presentation.dto.response;

import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;

import java.util.UUID;

public record AddMemberResponse(
        UUID userId,
        UUID classId,
        ClassRole classRole
) {
    public static AddMemberResponse from(ClassMembershipEntity entity) {
        return new AddMemberResponse(
                entity.getUser().getId(),
                entity.getClassEntity().getId(),
                entity.getClassRole()
        );
    }
}
