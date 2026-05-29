package com.portal.conecta.hub.module.course.application.command;

import java.util.UUID;

public record UpdateCourseCommand(
        UUID courseId,
        String name,
        String code
) {
}