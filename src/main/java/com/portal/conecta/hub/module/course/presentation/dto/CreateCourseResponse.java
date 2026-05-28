package com.portal.conecta.hub.module.course.presentation.dto;

import com.portal.conecta.hub.module.course.domain.model.CourseEntity;

import java.time.Instant;
import java.util.UUID;

public record CreateCourseResponse(
        UUID id,
        String name,
        String code,
        Instant createdAt
) {

    public static CreateCourseResponse from(CourseEntity courseEntity) {
        return new CreateCourseResponse(
                courseEntity.getId(),
                courseEntity.getName(),
                courseEntity.getCode(),
                courseEntity.getCreatedAt()
        );
    }

}