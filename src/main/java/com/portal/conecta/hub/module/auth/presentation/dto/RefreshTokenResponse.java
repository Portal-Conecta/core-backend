package com.portal.conecta.hub.module.auth.presentation.dto;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken,
        Long expiresIn
) {
}