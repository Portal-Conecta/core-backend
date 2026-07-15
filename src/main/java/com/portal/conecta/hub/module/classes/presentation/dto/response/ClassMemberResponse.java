package com.portal.conecta.hub.module.classes.presentation.dto.response;

import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;

import java.util.UUID;

public record ClassMemberResponse(
        UUID id,
        String name,
        ClassRole classRole
) {
    public static ClassMemberResponse from(ClassMembershipEntity membership){
        return new ClassMemberResponse(
                membership.getUser().getId(),
                membership.getUser().getName(),
                membership.getClassRole()
        );
    }
}
