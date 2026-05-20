package com.portal.conecta.hub.module.user.application.command;

import java.util.UUID;

public record DeactivateUserCommand(UUID targetUserId) {
}
