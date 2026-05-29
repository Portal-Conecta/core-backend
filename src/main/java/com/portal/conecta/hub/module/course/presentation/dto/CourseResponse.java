package com.portal.conecta.hub.module.course.presentation.dto;

import com.portal.conecta.hub.module.course.domain.model.CourseEntity;

import java.util.UUID;

public record CourseResponse(
        UUID id,
        String name,
        String code
) {

    public static CourseResponse from(CourseEntity entity){
        return new CourseResponse(
              entity.getId(),
              entity.getName(),
              entity.getCode()
        );
    }
}
