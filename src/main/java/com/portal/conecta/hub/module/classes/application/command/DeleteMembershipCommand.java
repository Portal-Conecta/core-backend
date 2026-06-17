package com.portal.conecta.hub.module.classes.application.command;

import java.util.UUID;

public record DeleteMembershipCommand(
        UUID classId,
        UUID userId
) {
}
