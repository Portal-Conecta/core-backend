package com.portal.conecta.hub.module.course.application.command;

import com.portal.conecta.hub.module.course.domain.exception.InvalidCourseDataException;

import java.util.UUID;

public record UpdateCourseCommand(
        UUID courseId,
        String name,
        String code
) {
    public static UpdateCourseCommand of(UUID courseId, String name, String code) {
        if (name == null && code == null) {
            throw new InvalidCourseDataException("");
        }
        return new UpdateCourseCommand(courseId, name, code);
    }
}