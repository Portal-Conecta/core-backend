package com.portal.conecta.hub.module.classes.presentation.dto.response;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;

import java.time.Instant;
import java.util.UUID;

public record RestoreClassResponse(
        UUID id,
        String name,
        Integer number,
        String shift,
        UUID courseId,
        boolean active,
        Instant deletedAt,
        Instant updatedAt
) {

    public static RestoreClassResponse from(ClassEntity entity){
        return new RestoreClassResponse(
                entity.getId(),
                entity.getName(),
                entity.getNumber(),
                entity.getShift().name(),
                entity.getCourse().getId(),
                entity.getDeletedAt() == null,
                entity.getDeletedAt(),
                entity.getUpdatedAt()
        );
    }
}
