package com.portal.conecta.hub.module.auth.application.command;

public record LogoutCommand(
        String refreshToken
) {
}
