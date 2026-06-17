package com.portal.conecta.hub.module.classes.presentation.dto.response;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.model.Shift;

import java.time.Instant;
import java.util.UUID;

public record ClassResponse(
        UUID id,
        Shift shift,
        Integer number,
        String name,
        boolean active,
        UUID courseId,
        Instant createdAt
) {

    public static ClassResponse from(ClassEntity entity){
        return new ClassResponse(
                entity.getId(),
                entity.getShift(),
                entity.getNumber(),
                entity.getName(),
                entity.isActive(),
                entity.getCourse().getId(),
                entity.getCreatedAt()
        );
    }
}
