package com.portal.conecta.hub.module.classes.application.command;

import java.util.UUID;

public record DemoteMemberCommand(
        UUID classId,
        UUID userId
) {
}
