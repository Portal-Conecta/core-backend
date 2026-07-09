package com.portal.conecta.hub.module.classes.application.command;

import com.portal.conecta.hub.module.classes.domain.model.ClassRole;

import java.util.List;
import java.util.UUID;

public record BulkAddMembersCommand(
        UUID classId,
        List<Item> members
) {
    public record Item (UUID userId, ClassRole classRole){

    }
}
