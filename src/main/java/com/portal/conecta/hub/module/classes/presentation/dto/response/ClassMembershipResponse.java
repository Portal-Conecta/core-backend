package com.portal.conecta.hub.module.classes.presentation.dto.response;

import com.portal.conecta.hub.module.classes.domain.model.ClassMembershipEntity;
import com.portal.conecta.hub.module.classes.domain.model.ClassRole;
import com.portal.conecta.hub.module.classes.domain.model.Shift;

import java.time.Instant;
import java.util.UUID;

public record ClassMembershipResponse(
        UUID id,
        String name,
        Shift shift,
        Integer number,
        boolean active,
        UUID courseId,
        Instant createdAt,
        ClassRole classRole
) {

    public static ClassMembershipResponse from(ClassMembershipEntity membership){
        var classEntity = membership.getClassEntity();
        return new ClassMembershipResponse(
                classEntity.getId(),
                classEntity.getName(),
                classEntity.getShift(),
                classEntity.getNumber(),
                classEntity.isActive(),
                classEntity.getCourse().getId(),
                classEntity.getCreatedAt(),
                membership.getClassRole()
        );
    }

}
