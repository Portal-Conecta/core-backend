package com.portal.conecta.hub.module.user.application.command;

import java.util.UUID;

public record UpdateUserCommand(
        UUID targetUserId,
        String name,
        String email,
        String avatarUrl
) {
}
