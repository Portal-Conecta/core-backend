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
            throw new InvalidCourseDataException("courseId é obrigatório");
        }

        if (name == null && code == null) {
            throw new InvalidCourseDataException("Pelo menos um dos campos name ou code deve ser informado");
        }

        if (name != null && name.isBlank()) {
            throw new InvalidCourseDataException("name não pode estar em branco");
        }

        if (code != null && code.isBlank()) {
            throw new InvalidCourseDataException("code não pode estar em branco");
        }

        return new UpdateCourseCommand(courseId, name, code);
    }
}