package com.portal.conecta.hub.module.classes.presentation.dto;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;
import com.portal.conecta.hub.module.classes.domain.model.Shift;

import java.time.Instant;
import java.util.UUID;

public record CreateClassResponse(
        UUID id,
        Shift shift,
        Integer number,
        String name,
        UUID courseId,
        Instant createdAt,
        Instant deletedAt
) {

    public static CreateClassResponse from (ClassEntity classEntity){
        return new CreateClassResponse(
                classEntity.getId(),
                classEntity.getShift(),
                classEntity.getNumber(),
                classEntity.getName(),
                classEntity.getCourse().getId(),
                classEntity.getCreatedAt(),
                classEntity.getDeletedAt()
        );
    }
}
