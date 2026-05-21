package com.portal.conecta.hub.module.classes.application.command;

import com.portal.conecta.hub.module.classes.domain.model.Shift;

import java.util.UUID;

public record CreateClassCommand(
        Shift shift,
        UUID courseId
) {
}
