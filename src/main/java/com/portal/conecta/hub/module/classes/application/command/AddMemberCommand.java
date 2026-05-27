package com.portal.conecta.hub.module.classes.application.command;

import com.portal.conecta.hub.module.classes.domain.model.ClassRole;

import java.util.UUID;

public record AddMemberCommand(
        UUID classId,
        UUID userId,
        ClassRole classRole
) {
}
