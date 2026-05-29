package com.portal.conecta.hub.module.classes.presentation.dto;

import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.user.domain.model.TypeUser;

import java.util.UUID;

public record PromoteMemberResponse(
        UUID userId,
        UUID classId,
        ClassRole classRole,
        TypeUser userType
) {
    public static PromoteMemberResponse from(ClassMembershipEntity membership) {
        return new PromoteMemberResponse(
                membership.getUser().getId(),
                membership.getClassEntity().getId(),
                membership.getClassRole(),
                membership.getUser().getTypeUser()
        );
    }
}
