package com.portal.conecta.hub.module.auth.application.result;

public record LoginResult(
        String accessToken,
        String refreshToken,
        Long expiresIn
) {
}
