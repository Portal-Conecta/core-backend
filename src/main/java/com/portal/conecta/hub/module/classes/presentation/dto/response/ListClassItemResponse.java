package com.portal.conecta.hub.module.classes.presentation.dto.response;

import com.portal.conecta.hub.module.classes.domain.model.ClassEntity;

import java.util.UUID;

public record ListClassItemResponse(
        UUID id,
        String name,
        Integer number,
        String shift,
        UUID courseId,
        boolean active
) {
    public static ListClassItemResponse from(ClassEntity entity){
        return new ListClassItemResponse(
                entity.getId(),
                entity.getName(),
                entity.getNumber(),
                entity.getShift().name(),
                entity.getCourse().getId(),
                entity.isActive()
        );
    }
}
