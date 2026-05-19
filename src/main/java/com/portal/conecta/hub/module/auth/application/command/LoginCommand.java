package com.portal.conecta.hub.module.auth.application.command;

public record LoginCommand(
        String email,
        String password
) {}