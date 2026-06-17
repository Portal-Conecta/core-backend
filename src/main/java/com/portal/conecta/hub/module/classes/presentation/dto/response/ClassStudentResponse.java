package com.portal.conecta.hub.module.classes.presentation.dto.response;

import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;

import java.util.UUID;

public record ClassStudentResponse(
        UUID id,
        String name
) {
    public static ClassStudentResponse from(ClassMembershipEntity membership){
        return new ClassStudentResponse(
                membership.getUser().getId(),
                membership.getUser().getName()
        );
    }
}
