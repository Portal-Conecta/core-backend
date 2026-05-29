package com.portal.conecta.hub.module.course.application.command;

import com.portal.conecta.hub.module.course.domain.exception.InvalidCourseDataException;

import java.util.UUID;

public record UpdateCourseCommand(
        UUID courseId,
        String name,
        String code
) {
    public static UpdateCourseCommand of(UUID courseId, String name, String code) {
        if (courseId == null) {
            throw new InvalidCourseDataException("courseId is required");
        }

        if (name == null && code == null) {
            throw new InvalidCourseDataException("At least one of name or code must be provided");
        }

        if (name != null && name.isBlank()) {
            throw new InvalidCourseDataException("name must not be blank");
        }

        if (code != null && code.isBlank()) {
            throw new InvalidCourseDataException("code must not be blank");
        }

        return new UpdateCourseCommand(courseId, name, code);
    }
}