package com.portal.conecta.hub.module.course.application.command;

public record CreateCourseCommand(
        String name,
        String code
) {
}