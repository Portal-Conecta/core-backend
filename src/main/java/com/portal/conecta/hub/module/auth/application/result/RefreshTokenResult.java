package com.portal.conecta.hub.module.auth.application.result;

public record RefreshTokenResult(
        String accessToken,
        String refreshToken,
        Long expiresIn
) {}