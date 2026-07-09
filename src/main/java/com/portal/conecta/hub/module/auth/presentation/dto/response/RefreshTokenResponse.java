package com.portal.conecta.hub.module.auth.presentation.dto.response;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken,
        Long expiresIn
) {
}