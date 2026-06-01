package com.portal.conecta.hub.module.classes.presentation.dto.response;

import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;

import java.util.UUID;

public record DemoteMemberResponse(
        UUID userId,
        UUID classId,
        ClassRole classRole,
        TypeUser userType
) {

    public static DemoteMemberResponse from(ClassMembershipEntity membership) {
        return new DemoteMemberResponse(
                membership.getUser().getId(),
                membership.getClassEntity().getId(),
                membership.getClassRole(),
                membership.getUser().getTypeUser()
        );
    }
}
