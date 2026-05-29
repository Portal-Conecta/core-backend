package com.portal.conecta.hub.module.course.presentation.dto;

import com.portal.conecta.hub.module.course.domain.model.CourseEntity;

import java.time.Instant;
import java.util.UUID;

public record UpdateCourseResponse(
        UUID id,
        String name,
        String code,
        Instant updatedAt
) {

    public static UpdateCourseResponse from(CourseEntity course) {
        return new UpdateCourseResponse(
                course.getId(),
                course.getName(),
                course.getCode(),
                course.getUpdatedAt()
        );
    }

}